// Bot Sharding
const { ShardingManager } = require('discord.js');
const { token } = require('./config.json');
const manager = new ShardingManager('./bot.js', { token: token });

manager.spawn();
manager.on('shardCreate', shard => console.log(`Launched Help Desk Shard ${shard.id}`));

/*
What is sharding?

As bots grow and are added to an increasing number of guilds,
some developers may find it necessary to break or split portions of their bots operations into separate logical processes.
As such, Discord gateways implement a method of user-controlled guild sharding which allows for splitting events across a number of gateway connections.
Guild sharding is entirely user controlled, and requires no state-sharing between separate connections to operate.

(https://discord.com/developers/docs/topics/gateway#sharding)
 */