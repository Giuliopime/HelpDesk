module.exports = async (client, guild) => {
	await client.guildSchema.findOneAndDelete({ guildID: guild.id }, (err) => {
		if(err) console.log(err)
	});
};