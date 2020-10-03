module.exports = {
    name: 'guilds',
    description: 'List the servers in which the bot is in',
    dev: true,
    async execute(message) {
        let guilds = message.client.guilds.cache.map(guild => `Server name: ${guild.name}, members: ${guild.members.cache.size}\n`);
        await message.channel.send(guilds)
    },
}