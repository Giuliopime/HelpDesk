module.exports = async (client, channel) => {
	try {
		// Check if the deleted channel is a guild channel
		if (!channel.guild) return;
		// Get data from the database
		const data = await client.guildSchema.findOne({ guildID: channel.guild.id });
		if (!data) return;

		// Check if the deleted channel was an #help-desk
		let hdIndex = data.helpDesks.findIndex(helpDesk => helpDesk.channelID === channel.id);
		if(hdIndex > -1) {
			data.helpDesks.splice(hdIndex, 1);
			// Remove the #help-desk from the database
			await client.guildSchema.updateOne({guildID: channel.guild.id}, { $set : { helpDesks: data.helpDesks }});
			// Remove the #help-desk from the cache
			client.helpDesksCache.set(channel.guild.id, data.helpDesks.map(helpDesk => helpDesk.channelID));
		}
	}
	catch(err) {
		console.log(err);
	}
};