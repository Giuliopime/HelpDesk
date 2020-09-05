const { client } = require('../../bot');
exports.get_stats = (req, res) => {
    res.json({guilds: client.guilds.cache.size, users: client.users.cache.size});
};
exports.get_commands = (req, res) => {
    let commandList = client.commands.map(command => {
        return {
            name: command.name,
            description: command.description,
            aliases: command.aliases,
            usage: command.usage,
            cooldown: command.cooldown,
            guildOnly: command.guildOnly,
            embed: command.embed,
            helpdesk: command.helpdesk,
            utility: command.utility,
        }
    });
    res.json(commandList);
}
