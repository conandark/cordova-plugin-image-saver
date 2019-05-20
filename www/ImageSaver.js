function ImageSaver() {}

ImageSaver.prototype.saveImageToLibrary = function (data, successCallback, failureCallback) {
    cordova.exec(successCallback, failureCallback, "ImageSaver", "saveImageToLibrary", [data]);
};

module.exports = new ImageSaver();