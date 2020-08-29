module.exports = async (client, channel) => {
	try {
		// Check the channel deleted is a guild channel
		if (!channel.guild) return;
		// Get all the variables
		const data = await client.guildSchema.findOne({ guildID: channel.guild.id });
		const channelID = channel.id;
		if (!data) return;
		// Check if anything saved in the database got deleted
		let hdIndex = data.helpDesks.findIndex(helpDesk => helpDesk.channelID === channelID);
		if(hdIndex > -1) {
			data.helpDesks.splice(hdIndex, 1);
			await client.guildSchema.updateOne({guildID: channel.guild.id}, { $set : { helpDesks: data.helpDesks }});
			client.helpDesksCache.set(channel.guild.id, data.helpDesks.map(helpDesk => helpDesk.channelID));
		}

	}
	catch(err) {
		console.log(err);
	}
};