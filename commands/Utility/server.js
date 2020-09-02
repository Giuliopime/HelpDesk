module.exports = {
	// Info
	name: 'server',
	description: 'Get a link to Help Desk\'s Support Server.',
	aliases: ['support'],
	cooldown: 3,
	// Basic checks
	guildOnly: false,
	// Command Category
	utility: true,
	async execute(data, member, message) {
		message.channel.send('https://discord.gg/4BTXnXu');
	},
};