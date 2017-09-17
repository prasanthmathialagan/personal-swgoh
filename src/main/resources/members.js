const swgoh = require("swgoh").swgoh

const username= "prasanthswgoh";
swgoh.profile(username).then(function (p) {
	return swgoh.guild(p.guildUrl);
}).then(function (members) {
	for (var i in members) {
		var member = members[i];
		console.log(member.username + "," + member.description);
	}
});
