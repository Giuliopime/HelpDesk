const fs = require('fs');

module.exports = {
	// Info
	name: 'save',
	description: 'Get a json template of your help-desk',
	aliases: ['savesettings'],
	cooldown: 5,
	// Basic checks
	guildOnly: true,
	chooseDesk: true,
	// Command Category
	helpdesk: true,
	// Permissions needed
	perms: ['ATTACH_FILES'],
	async execute(data, member, message, args, deskIndex) {
		const helpDesk = data.helpDesks[deskIndex];
		const fileName = './helpDesk_' + helpDesk.channelID + '.json';
		delete helpDesk.channelID;
		delete helpDesk.messageID;
		const jsonToSend = JSON.stringify(helpDesk);
		fs.writeFile(fileName, jsonToSend, async (err) => {
			if(err) {
				console.log(err);
				return message.channel.send(message.client.errorEmbed.setDescription('Unexpected error.'));
			}
			message.channel.send('You can save this file and re-upload it to the bot if you are gonna use those help-desk settings in the future or in another server via the `hd?load` command.', {
				files: [
					fileName,
				],
			})
				.then(()=> fs.unlinkSync(fileName))
				.catch(()=> fs.unlinkSync(fileName));
		});
	},
};