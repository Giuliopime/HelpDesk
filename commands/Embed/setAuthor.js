module.exports = {
    // Info
    name: 'setauthor',
    description: 'Set an author to for the #help-desk embed',
    aliases: ['sauthor'],
    args: /.+/,
    usage: '<author> or <{delete}> to remove',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    embed: true,
    // Permissions needed
    perms: ['EMBED_LINKS'],
    async execute(data, member, message, args, index) {
        let text = args.join(' ');
        if(text.length > 256) {
            return message.channel.send(message.client.errorEmbed.setDescription('Discord allows up to 256 characters for author names.\nCheck all embed limits on [this link](https://discord.com/developers/docs/resources/channel#embed-limits).'))
        }
        if(args[0] === '{delete}') text = undefined;
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.author.name']: text } });
        message.client.replyEmbed.setDescription('Author set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};