#import "ReceiveSmsPlugin.h"
#if __has_include(<receive_sms/receive_sms-Swift.h>)
#import <receive_sms/receive_sms-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "receive_sms-Swift.h"
#endif

@implementation ReceiveSmsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftReceiveSmsPlugin registerWithRegistrar:registrar];
}
@end
