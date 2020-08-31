module.exports = {
    // Info
    name: 'settimestamp',
    description: 'Set a timestamp as the current date for the #help-desk embed',
    aliases: ['stimestamp'],
    args: false,
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    embed: true,
    // Permissions needed
    perms: ['EMBED_LINKS'],
    async execute(data, member, message, args, index) {
        let text = Date.now();
        if(args[0] === '{delete}') text = undefined;
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.timestamp']: text } });
        message.client.replyEmbed.setDescription('Timestamp set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};