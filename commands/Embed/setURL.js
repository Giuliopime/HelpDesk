module.exports = {
    // Info
    name: 'seturl',
    description: 'Set an url for the title of the #help-desk embed',
    aliases: ['surl'],
    args: true,
    usage: '<URL> or <{delete}> to remove',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    embed: true,
    async execute(data, member, message, args, index) {
        let url = args[0];
        if(args[0] === '{delete}') url = undefined;
        const isValid = message.client.isValidURL(url);
        if(!isValid && url !== '{delete}') {
            message.client.errorEmbed.setDescription('You need to provide a correct URL.\nAlternatively you can use `{delete}` to remove the image from the embed.');
            return message.channel.send(message.client.errorEmbed);
        }
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.url']: url } });
        message.client.replyEmbed.setDescription('Title URL set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};