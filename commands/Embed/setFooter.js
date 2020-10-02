module.exports = {
    // Info
    name: 'setfooter',
    description: 'Set a footer to for the #help-desk embed',
    aliases: ['sfooter'],
    args: /(.+)/,
    usage: '<footer / {delete} to remove>',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    embed: true,
    // Permissions needed
    perms: ['EMBED_LINKS'],
    async execute(data, member, message, args, index) {
        let text = args.join(' ');
        if(text.length > 2048) {
            return message.channel.send(message.client.errorEmbed.setDescription('Discord allows up to 2048 characters for embed footers.\nCheck all embed limits on [this link](https://discord.com/developers/docs/resources/channel#embed-limits).'))
        }
        if(args[0] === '{delete}') text = undefined;
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.footer']: text } });
        message.client.replyEmbed.setDescription('Footer set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};