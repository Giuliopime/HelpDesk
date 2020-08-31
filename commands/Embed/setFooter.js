module.exports = {
    // Info
    name: 'setfooter',
    description: 'Set a footer to for the #help-desk embed',
    aliases: ['sfooter'],
    args: true,
    usage: '<footer> or <{delete}> to remove',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    embed: true,
    // Permissions needed
    perms: ['EMBED_LINKS'],
    async execute(data, member, message, args, index) {
        let text = args.join(' ');
        if(args[0] === '{delete}') text = undefined;
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.footer']: text } });
        message.client.replyEmbed.setDescription('Footer set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};