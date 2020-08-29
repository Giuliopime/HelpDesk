const Discord = require('discord.js');

module.exports = {
	// Info
	name: 'invite',
	description: 'Invite Astro to upgrade your server experience!',
	args: false,
	cooldown: 20,
	// Basic checks
	guildOnly: false,
	// Command Category
	utility: true,
	async execute(data, member, message) {
		const inviteEmbed = new Discord.MessageEmbed()
			.setColor('#000000')
			.setTitle('**Click Here to invite Help Desk to your server!**')
			.setURL('https://discord.com/oauth2/authorize?client_id=739796627681837067&scope=bot&permissions=268954832');
		await message.channel.send(inviteEmbed);
	},
};