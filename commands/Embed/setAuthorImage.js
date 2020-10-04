module.exports = {
	// Info
	name: 'setauthorimage',
	description: 'Set an author image to for the #help-desk embed',
	aliases: ['sauthorimage'],
	args: /^((https?:\/\/)?((([a-z\d]([a-z\d-]*[a-z\d])*)\.)+[a-z]{2,}|((\d{1,3}\.){3}\d{1,3}))(:\d+)?(\/[-a-z\d%_.~+]*)*(\?[;&a-z\d%_.~+=-]*)?(#[-a-z\d_]*)?|{delete})$/i,
	usage: '<URL / {delete} to remove>',
	cooldown: 5,
	// Basic checks
	guildOnly: true,
	// Command Category
	embed: true,
	// Permissions needed
	perms: ['EMBED_LINKS'],
	async execute(data, member, message, args, index) {
		let url = args[0];
		if(args[0] === '{delete}') url = undefined;
		await message.client.guildSchema.updateOne({ guildID: message.guild.id }, { $set:{ ['helpDesks.' + index + '.embedProperties.author.icon_url']: url } });
		await message.client.caches.hdel('settings', message.guild.id);

		message.client.replyEmbed.setDescription('Author Image set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
		await message.channel.send(message.client.replyEmbed);
	},
};