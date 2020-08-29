const Discord = require('discord.js');

module.exports = {
	// Info
	name: 'help',
	description: 'General info about Help Desk & Commands',
	args: false,
	cooldown: 3,
	// Basic checks
	guildOnly: false,
	// Command Category
	utility: true,
	async execute(data, member, message, args) {
		const prefix = 'hd?';
		let helpEmbed = new Discord.MessageEmbed()
			.setColor('#000000');

		// Command Help
		const command = args[0] ? message.client.commands.get(args[0].toLowerCase()) || message.client.commands.find(c => c.aliases && c.aliases.includes(args[0].toLowerCase())) : undefined;
		if(command) {
			helpEmbed.setAuthor(`Help Command: ${command.name}`, `${message.client.user.displayAvatarURL()}`)
				.setDescription(command.description)
				.addFields(
					{name: `**Usage**`, value: `\`${prefix + command.name + (command.usage ? ' '+command.usage : '')}\``},
					{name: `**Aliases**`, value: command.aliases ? `\`${command.aliases.join(',')}\`` : '*No aliases*'},
					{name: `**Cooldown**`, value: `${command.cooldown} seconds`},
				);
		}

		// Category Help
		let category, categoryName;
		if(args[0]) {
			switch (args[0].toLowerCase()) {
				case 'help-desk':
					category = 'helpdesk';
					categoryName = 'Help Desk'
					break;
				case 'embed':
					category = 'embed';
					categoryName = 'Embed'
					break;
				case 'utility':
					category = 'utility';
					categoryName = 'Utility'
					break;
			}
		}
		if(category && !command) {
			helpEmbed.setAuthor(`Category`, `${message.client.user.displayAvatarURL()}`)
				helpEmbed.addField(`${categoryName} commands`, `\`${message.client.commands.filter(cmd => cmd[category]).map(cmd => cmd.name).join('\`, \`')}\`\n\u200b`)
				.setFooter(`Type 'hd?help <CommandName>' for details on a command`);
		}

		// General help
		if(!command && !category) {
			helpEmbed.setAuthor(`Help Command`, `${message.client.user.displayAvatarURL()}`)
				.addFields(
					{name: `Help-Desk Commands`, value: `\`${message.client.commands.filter(cmd => cmd.helpdesk).map(cmd => cmd.name).join('\`, \`')}\``},
					{name: `Embed Commands`, value: `\`${message.client.commands.filter(cmd => cmd.embed).map(cmd => cmd.name).join('\`, \`')}\``},
					{name: `Utility commands`, value: `\`${message.client.commands.filter(cmd => cmd.utility).map(cmd => cmd.name).join('\`, \`')}\``},
					{name: `Useful Links`, value: `[Support](https://discord.gg/4BTXnXu) | [Invite](https://discord.com/oauth2/authorize?client_id=739796627681837067&scope=bot&permissions=268954832)`},
				)
				.setFooter(`Type 'hd?help <CommandName>' for details on a command`);
		}
		await message.channel.send(helpEmbed);
	}
};