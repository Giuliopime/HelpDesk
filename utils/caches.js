// Node Module
const redis = require('redis');
// Promisify is used to convert redis methods into promises
// So that we can use .then() instead of callbacks
const { promisify } = require('util');

/* Here hashes are used to store different kind of data
   What is stored:
    Global User Cooldown
    Command Cooldown
    Guild Settings
*/
const caches = redis.createClient();
const hset = promisify(caches.hmset).bind(caches);
const hget = promisify(caches.hget).bind(caches);
const hdel = promisify(caches.hdel).bind(caches);
const saveCache = promisify(caches.bgsave).bind(caches);
caches.on('error', (error) => console.error(error));

// Export the caches and methods as an objects
module.exports.caches = {
	caches: caches,
	hset: hset,
	hget: hget,
	hdel: hdel,
	saveCache: saveCache,
};

console.log('Help Desk\'s Cache Loaded');