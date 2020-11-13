#import "Cordova/CDV.h"
#import "AppIconChanger.h"

@implementation AppIconChanger

- (void) isSupported:(CDVInvokedUrlCommand*)command
{
  CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:[self supportsAlternateIcons]];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) changeIcon:(CDVInvokedUrlCommand*)command
{
  if (![self supportsAlternateIcons]) {
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"This version of iOS doesn't support alternate icons"] callbackId:command.callbackId];
    return;
  }

  NSDictionary* options = command.arguments[0];
  NSString *iconName = options[@"iconName"];
  BOOL suppressUserNotification = (options[@"suppressUserNotification"] == nil || [options[@"suppressUserNotification"] boolValue]);

  [[UIApplication sharedApplication] setAlternateIconName:iconName completionHandler:^(NSError *error) {
      if (error != nil) {
        NSString *errMsg = error.localizedDescription;
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errMsg] callbackId:command.callbackId];
      } else {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
      }
  }];


  if (suppressUserNotification) {
    [self suppressUserNotification];
  }
}


#pragma mark - Helper functions

- (BOOL) supportsAlternateIcons
{
    return [[UIApplication sharedApplication] supportsAlternateIcons];
}

- (void) suppressUserNotification
{
  UIViewController* suppressAlertVC = [UIViewController new];
  [suppressAlertVC setModalPresentationStyle: UIModalPresentationFullScreen];
  [self.viewController presentViewController:suppressAlertVC animated:NO completion:^{
          [suppressAlertVC dismissViewControllerAnimated:NO completion: nil];
      }];
}

@end
