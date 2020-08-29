module.exports = {
    // Info
    name: 'setthumbnail',
    description: 'Set a thumbnail to for the #help-desk embed',
    aliases: ['sthumbnail'],
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
            message.client.errorEmbed.setDescription('You need to provide a correct URL for an image.\nYou can use tools like [imgbb](https://it.imgbb.com/) to get a permanent URL for an image.\n\nAlternatively you can use `{delete}` to remove the image from the embed.');
            return message.channel.send(message.client.errorEmbed);
        }
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.thumbnail.url']: url } });
        message.client.replyEmbed.setDescription('Thumbnail set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};