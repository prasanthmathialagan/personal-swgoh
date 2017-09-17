const swgoh = require("swgoh").swgoh

const username= "prasanthswgoh";

swgoh.profile(username).then(function (p) {
	return swgoh.guild(p.guildUrl);
}).then(function (members) {
	for (var i in members) {
		var member = members[i];
		swgoh.profile(member.username).then(function (k) {
			swgoh.collection(k.username).then(function (t) {
				var toon = k.username;
				for (var i in t) {
					val = t[i];
					toon = toon + "," + val.description + "," + val.star + "," + val.galacticPower;
				}
				console.log(toon);
			});
		});
	}
});
