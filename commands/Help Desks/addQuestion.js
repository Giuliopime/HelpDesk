module.exports = {
	// Info
	name: 'addquestion',
	description: 'Add a question to the #help-desk embed',
	aliases: ['aquestion'],
	args: /^(.+?)( *\|\|\| *)([\s\S]+)$/,
	usage: '<question> ||| <answer>',
	cooldown: 5,
	// Basic checks
	guildOnly: true,
	chooseDesk: true,
	// Command Category
	helpdesk: true,
	async execute(data, member, message, args, index) {
		if(data.helpDesks[index].embedProperties.fields.length >= 9) {
			message.client.errorEmbed.setDescription('I don\'t currently support more than 9 questions.');
			return message.channel.send(message.client.errorEmbed);
		}
		if(args[0].length > 1024) {
			message.client.errorEmbed.setDescription('The question can\'t be longer than 1024 characters.');
			return message.channel.send(message.client.errorEmbed);
		}
		if(args[2].length > 1024) {
			message.client.errorEmbed.setDescription('The answer can\'t be longer than 1024 characters.');
			return message.channel.send(message.client.errorEmbed);
		}
		data.helpDesks[index].embedProperties.fields.push(args[0]);
		data.helpDesks[index].fieldsReplies.push(args[2]);
		await message.client.guildSchema.updateOne({ guildID: message.guild.id }, { $set: { ['helpDesks.' + index]: data.helpDesks[index] } });
		await message.client.caches.hdel('settings', message.guild.id);

		message.client.replyEmbed.setDescription('Question added. Use `hd?update` to apply the changes.');
		await message.channel.send(message.client.replyEmbed);
	},
};