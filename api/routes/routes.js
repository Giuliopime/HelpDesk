module.exports = (app) => {
    // Controller
    const controller = require('../controllers/controller');

    // Routes
    app.route('/stats').get(controller.get_stats);
    app.route('/commands').get(controller.get_commands);
};