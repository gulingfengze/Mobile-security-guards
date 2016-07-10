package receiver;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

/**
 * 桌面小组件（锁屏清理广播）
 */
public class ClearTaskReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		 /*获取正在运行的所有进程*/
		List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
			am.killBackgroundProcesses(runningAppProcessInfo.processName);//清理进程
			System.out.println("锁屏清理进程");
		}
	}

}
