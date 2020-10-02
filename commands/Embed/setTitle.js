module.exports = {
    // Info
    name: 'settitle',
    description: 'Set a title to for the #help-desk embed',
    aliases: ['stitle'],
    args: /(.+)/,
    usage: '<title / {delete} to remove>',
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
            return message.channel.send(message.client.errorEmbed.setDescription('Discord allows up to 256 characters for embed titles.\nCheck all embed limits on [this link](https://discord.com/developers/docs/resources/channel#embed-limits).'))
        }
        if(args[0] === '{delete}') text = undefined;
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.title']: text } });
        message.client.replyEmbed.setDescription('Title set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};