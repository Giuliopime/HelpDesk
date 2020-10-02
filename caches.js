// Bot redis caches
const redis = require("redis");
const { promisify } = require("util");

// Global Cooldown cache
const gCDCache = redis.createClient();
const setGCD = promisify(gCDCache.set).bind(gCDCache);
const getGCD = promisify(gCDCache.get).bind(gCDCache);
const existsGCD = promisify(gCDCache.exists).bind(gCDCache);
const delGCD = promisify(gCDCache.del).bind(gCDCache);
gCDCache.on("error", (error) => {
    console.error(error);
});

const caches = {
    gCDCache: gCDCache,
    setGCD: setGCD,
    getGCD: getGCD,
    existsGCD: existsGCD,
    delGCD: delGCD,
}

module.exports.caches = caches;

console.log('Help Desk\'s Caches Loaded');