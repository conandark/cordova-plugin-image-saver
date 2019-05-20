#import <Cordova/CDVPlugin.h>

@interface ImageSaver : CDVPlugin
{
    NSString* callbackId;
}

@property (nonatomic, copy) NSString* callbackId;

- (void)saveImageToLibrary:(CDVInvokedUrlCommand*)command;

@end
