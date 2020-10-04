// Node Modules
const fs = require('fs');
const path = require('path');

module.exports = {
	name: 'reload',
	description: 'Reload a command',
	aliases: ['rl'],
	args: /^(.+)$/,
	dev: true,
	execute(message, args) {
		const commandName = args[0].toLowerCase();
		const command = message.client.commands.get(commandName)
            || message.client.commands.find(cmd => cmd.aliases && cmd.aliases.includes(commandName));

		if (!command) return message.channel.send(`There is no command with name or alias \`${commandName}\`, ${message.author}!`);

		const commandFile = traverse(path.join(__dirname, '../../commands'), commandName);
		if(!commandFile) return message.channel.send('File not found');
		delete require.cache[require.resolve(commandFile)];

		try {
			const newCommand = require(commandFile);
			message.client.commands.set(newCommand.name, newCommand);
			message.channel.send(`Command \`${command.name}\` was reloaded!`);
		}
		catch (error) {
			console.log(error);
			message.channel.send(`There was an error while reloading a command \`${command.name}\`:\n\`${error.message}\``);
		}
	},
};

function traverse(dir, filename) {
	for (const dirent of fs.readdirSync(dir, { withFileTypes: true })) {
		const direntPath = path.join(dir, dirent.name);
		if (dirent.isDirectory()) {
			const result = traverse(direntPath, filename);
			if(result) return result;
		}
		else if(dirent.name === filename + '.js') {return direntPath;}
	}
	return null;
}