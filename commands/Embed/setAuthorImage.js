module.exports = {
    // Info
    name: 'setauthorimage',
    description: 'Set an author image to for the #help-desk embed',
    aliases: ['sauthorimage'],
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
        if(!isValid && args[0] !== '{delete}') {
            message.client.errorEmbed.setDescription('You need to provide a correct URL for an image.\nYou can use tools like [imgbb](https://imgbb.com/) to get a permanent URL for an image.\nWhen using imgbb make sure to select the \'HTML codes\'->\'HTML full linked\'->copy the url in the src="" attribute of the <img> tag.\n\nAlternatively you can use `{delete}` to remove the image from the embed.');
            return message.channel.send(message.client.errorEmbed);
        }
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.author.icon_url']: url } });
        message.client.replyEmbed.setDescription('Author Image set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};