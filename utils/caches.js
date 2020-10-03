// Node Module
const redis = require("redis");
// Promisify is used to convert redis methods into promises
// So that we can use .then() instead of callbacks
const { promisify } = require("util");

// Global Cooldown Cache & methods
const gCDCache = redis.createClient();

const setGCD = promisify(gCDCache.set).bind(gCDCache);
const getGCD = promisify(gCDCache.get).bind(gCDCache);
const existsGCD = promisify(gCDCache.exists).bind(gCDCache);
const delGCD = promisify(gCDCache.del).bind(gCDCache);
gCDCache.on("error", (error) => {
    console.error(error);
});

// Export the caches and methods as an objects
module.exports.caches = {
    gCDCache: gCDCache,
    setGCD: setGCD,
    getGCD: getGCD,
    existsGCD: existsGCD,
    delGCD: delGCD,
}

console.log('Help Desk\'s Cache Loaded');