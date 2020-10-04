module.exports = async (client, channel) => {
	try {
		// Check if the deleted channel is a guild channel
		if (!channel.guild) return;
		// Get data from the cache or database if guild settings are not cached yet
		let data = await client.caches.hget('settings', channel.guild.id);
		if (!data) {
			data = await client.guildSchema.findOne({ guildID: channel.guild.id });
			if(!data) return;
		}
		else {data = JSON.parse(data);}

		// Check if the deleted channel was an #help-desk
		const hdIndex = data.helpDesks.findIndex(helpDesk => helpDesk.channelID === channel.id);
		if(hdIndex > -1) {
			data.helpDesks.splice(hdIndex, 1);
			// Remove the #help-desk from the database
			await client.guildSchema.updateOne({ guildID: channel.guild.id }, { $set : { helpDesks: data.helpDesks } });
			// Remove the #help-desk from the cache
			await client.caches.hdel('settings', channel.guild.id);
		}
	}
	catch(err) {
		console.log(err);
	}
};