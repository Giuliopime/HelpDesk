module.exports = {
	name: 'destroy',
	description: 'Turn off the Bot',
	args: true,
	dev: true,
	async execute(message) {
		await message.client.guildSchema.findOne({ guildID: 'stoppedServers' }, async (_, res)=>{
			if(res) {
				res.generators = message.client.stoppedServers;
				res.save().catch();
			}
			else{
				// Create the new guild object for the database
				const newGuild = new message.client.guildSchema({
					guildID: 'stoppedServers',
					prefix: undefined,
					setup: undefined,
					commChat: undefined,
					moderation: undefined,
					generators: message.client.stoppedServers,
					controls: undefined,
					role: undefined,
					vcs: undefined,
					links: undefined,
				});
				// Save the object in the database
				await newGuild.save().catch(err => console.log(err));
			}
		});
		await message.channel.send('Turning off...');
		await message.client.destroy();
	},
};