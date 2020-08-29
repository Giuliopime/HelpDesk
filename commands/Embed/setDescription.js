module.exports = {
    // Info
    name: 'setdescription',
    description: 'Set a description to for the #help-desk embed',
    aliases: ['sdescription'],
    args: true,
    usage: '<description> or <{delete}> to remove',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    embed: true,
    async execute(data, member, message, args, index) {
        let text = args.join(' ');
        if(args[0] === '{delete}') text = undefined;
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.description']: text } });
        message.client.replyEmbed.setDescription('Description set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};