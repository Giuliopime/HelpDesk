module.exports = {
    name: 'guilds',
    description: 'List the servers',
    dev: true,
    async execute(message, args) {
        let guilds = message.client.guilds.cache.map(guild => `Server name: ${guild.name}, members: ${guild.members.cache.size}\n`);
        await message.channel.send(guilds)
    },
}