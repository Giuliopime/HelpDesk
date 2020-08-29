module.exports = {
    // Info
    name: 'setauthorurl',
    description: 'Set an image to for the #help-desk embed',
    aliases: ['sauthorurl'],
    args: true,
    usage: '<imageURL> or <{delete}> to remove',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    embed: true,
    async execute(data, member, message, args, index) {
        let url = args[0];
        if(args[0] === '{delete}') url = undefined;
        const isValid = message.client.isValidURL(url);
        if(!isValid && args[0] !== '{delete}') {
            message.client.errorEmbed.setDescription('You need to provide a correct URL.\nAlternatively you can use `{delete}` to remove the image from the embed.');
            return message.channel.send(message.client.errorEmbed);
        }
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.author.url']: url } });
        message.client.replyEmbed.setDescription('Author URL set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};