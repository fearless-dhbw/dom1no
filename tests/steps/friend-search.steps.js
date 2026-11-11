const assert = require("node:assert/strict");
const { Given, When, Then, Before } = require("@cucumber/cucumber");

const demoPlayers = {
  PLAYER1: { username: "PLAYER1", displayName: "Player One" },
  PLAYER2: { username: "PLAYER2", displayName: "Player Two" },
};

Before(function () {
  this.demo = {
    currentPlayer: null,
    section: null,
    searchResults: [],
    viewedPlayer: null,
  };
});

Given("I am signed in as {string}", function (username) {
  this.demo.currentPlayer = username;
  assert.ok(this.demo.currentPlayer);
});

Given("I am in the Friends section", function () {
  this.demo.section = "Friends";
});

When("I search for the username {string}", function (username) {
  assert.ok(this.demo.currentPlayer);
  assert.equal(this.demo.section, "Friends");
  const query = username.toLowerCase();
  const currentUsername = this.demo.currentPlayer.toLowerCase();
  this.demo.searchResults = Object.values(demoPlayers).filter((player) => {
    const candidateUsername = player.username.toLowerCase();
    return candidateUsername !== currentUsername && candidateUsername.includes(query);
  });
});

Then("the system displays {string} in the search results", function (username) {
  assert.ok(this.demo.searchResults.some((player) => player.username === username));
});

Then("the search results do not include {string}", function (username) {
  assert.equal(this.demo.searchResults.some((player) => player.username === username), false);
});

Then("I can view that player's information", function () {
  const [player] = this.demo.searchResults;
  assert.ok(player);
  this.demo.viewedPlayer = player;
  assert.equal(this.demo.viewedPlayer.displayName, "Player Two");
});
