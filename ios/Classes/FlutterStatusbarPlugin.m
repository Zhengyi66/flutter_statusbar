#import "FlutterStatusbarPlugin.h"
#import <flutter_statusbar/flutter_statusbar-Swift.h>

@implementation FlutterStatusbarPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterStatusbarPlugin registerWithRegistrar:registrar];
}
@end
