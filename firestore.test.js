const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_hacker_1";
const BOB_UID = "bob_hacker_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated caller: cannot read or write any user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      handle: "UNAUTH",
      lifetimeXp: 100,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      handle: "ALICE_ROOT",
      lifetimeXp: 500,
      contractsCompleted: 3,
      solvedQuestIds: ["first-boot"],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read or modify another user's profile", async () => {
  // Seed Alice data
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      handle: "ALICE",
      lifetimeXp: 1000,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      handle: "PWNED",
      updatedAt: new Date(),
    })
  );
});

test("Custom quests: user can create and list their own quest mods, but not another user's", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_quests").doc("mod_1").set({
      userId: ALICE_UID,
      questId: "mod_1",
      title: "Alice Mod",
      cabinetId: "sudo-hero",
      description: "Custom arena",
      author: "Alice",
      rawScript: "@quest id=\"mod_1\" title=\"Alice Mod\"\n@step choice xp=100\nprompt: test\nchoice: a\nchoice: b\nanswer: 0",
      stepCount: 1,
      maxXp: 100,
      enemyCount: 4,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_quests").get()
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("custom_quests").doc("mod_1").get()
  );
});

test("Events log: append-only, cannot update or delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("events_log").doc("evt_1").set({
      userId: ALICE_UID,
      type: "quest_started",
      roomCode: "SKOG",
      description: "Quest started",
      xpDelta: 100,
      createdAt: new Date(),
    })
  );

  // Update must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("events_log").doc("evt_1").update({
      description: "Forged log",
    })
  );

  // Delete must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("events_log").doc("evt_1").delete()
  );
});
