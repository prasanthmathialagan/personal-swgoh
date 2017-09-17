const swgoh = require("swgoh").swgoh

// -----------------------------------------------------------------------
// Get the list of all toons
// mrrush --> This guys is a top arena player and has all the toons.
const reference = "mrrush"

swgoh.collection(reference).then(function (t) {
	for (var i in t) {
		val = t[i];
		console.log(val.description+ "," + val.code);
	}
});

// -----------------------------------------------------------------------
