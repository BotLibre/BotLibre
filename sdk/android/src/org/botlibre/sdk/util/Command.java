package org.botlibre.sdk.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.botlibre.sdk.activity.MainActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.MediaStore;

@SuppressLint({ "DefaultLocale", "NewApi" })
public class Command {
	private Context context;
	private JSONObject jsonObject;
	private PackageManager manager;
	private boolean stimeExists = false;
	private boolean etimeExists = false;

	public Command(Context context, JSONObject jsonObject) {

		manager = context.getPackageManager();

		if (jsonObject != null) {
			this.jsonObject = jsonObject;
			this.context = context;

			try {
				String type = (String)jsonObject.get("type");
				if (type != null) {
					if (type.equalsIgnoreCase("intent")) {
						intent();
					} else if (type.equalsIgnoreCase("email")) {
						email();
					} else if (type.equalsIgnoreCase("open")) {
						openApp();
					} else if (type.equalsIgnoreCase("phone")) {
						phone();
					} else if (type.equalsIgnoreCase("sms")) {
						sms();
					} else if (type.equalsIgnoreCase("alarm")) {
						alarm();
					} else if (type.equalsIgnoreCase("calendar")) {
						calendar();
					} else if (type.equalsIgnoreCase("map")) {
						map();
					} else if (type.equalsIgnoreCase("camera")) {
						camera();
					} else if (type.equalsIgnoreCase("google")) {
						google();
					}
				} 

			} catch (Exception exception) {
				MainActivity.error(exception.getMessage(), exception, (Activity) context);
			}

		}
	}

	public void intent()  {

		System.out.println(jsonObject.toString());

		PackageManager manager = context.getPackageManager();

		String action = (String) jsonObject.opt("action");
		String uri = ((String) jsonObject.opt("uri"));
		String thePackage = (String) jsonObject.opt("package");
		String dataType = (String) jsonObject.opt("dataType");

		boolean result = false;
		int requestCode = 0;

		Intent intent = new Intent(action);

		if (uri != null) {
			
			intent.setData(Uri.parse(uri.replace(" ", "").replace("at", "@").replace("-", "")));
		}
		if (thePackage != null) {
			intent.setPackage(thePackage);
		}
		if (dataType != null) {
			intent.setType(dataType);
		}

		if (jsonObject.has("extra")) {
			JSONArray extras = (JSONArray) jsonObject.opt("extra");

			for (int i = 0; i < extras.length(); i++) {
				JSONObject extraObject;
				try {
					extraObject = (JSONObject) extras.get(i);
					String type = (String)extraObject.opt("EXTRA_TYPE");
					String extraKey = (String) extraObject.opt("EXTRA_KEY");

					if (type != null && type.equalsIgnoreCase("int")) {
						int extraValue = (Integer) extraObject.optInt("EXTRA_VALUE");
						intent.putExtra(extraKey, extraValue);

					} else if (type != null && type.equalsIgnoreCase("result")) {
						result = true;
						String extraValue = (String) extraObject.opt("EXTRA_VALUE");
						requestCode = (Integer) extraObject.optInt("CODE");
						if (extraValue != null) {
							intent.putExtra(extraKey, extraValue);
						}

					} else {
						String extraValue = (String) extraObject.opt("EXTRA_VALUE");
						intent.putExtra(extraKey, extraValue);
					}
				} catch (JSONException e) {
					MainActivity.error(e.getMessage(),e, (Activity) context);
				}

			}
		}

		if (intent.resolveActivity(manager) != null && (result == true)) {
			((Activity) context).startActivityForResult(intent, requestCode);
		} else if (intent.resolveActivity(manager) != null && (result == false)){
			context.startActivity(intent);
		} else {
			MainActivity.showMessage("This command/program is not available on your device", (Activity)context);
		}

	}

	public void email() {

		String action = (String) jsonObject.opt("action");
		String address = ((String) jsonObject.opt("address")).replace("at", "@");
		String subject = (String) jsonObject.opt("subject");
		String message = (String) jsonObject.opt("message");

		if (action.equalsIgnoreCase("send") && action != null) {

			Intent send = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));

			if (address != null) {
				send.setData(Uri.parse("mailto:"+(address.replace(" ", ""))));
			} 
			if (subject != null) {
				send.putExtra(Intent.EXTRA_SUBJECT, subject);
			}
			if (message != null) {
				send.putExtra(Intent.EXTRA_TEXT, message);
			}
			if (send.resolveActivity(manager) != null) {
				context.startActivity(send);
			} else {
				MainActivity.showMessage("Email commands not available on your device", (Activity)context);
			}

		}
	}

	public void openApp() {

		String thePackage = (String) jsonObject.opt("package");


		try {
			Intent intent = manager.getLaunchIntentForPackage(thePackage);
			if (intent != null) {
				context.startActivity(intent);
			} else {
				throw new PackageManager.NameNotFoundException();
			}
		} catch (PackageManager.NameNotFoundException e) {
			MainActivity.showMessage("This program is not available on your device", (Activity)context);
		}

	}

	public void calendar() {
		String action = (String) jsonObject.opt("action");
		String location = (String) jsonObject.opt("location");
		String begin = (String) jsonObject.opt("begin");
		String end = (String) jsonObject.opt("end");
		String name = (String) jsonObject.opt("name");

		SimpleDateFormat format;

		int year = Calendar.getInstance().get(Calendar.YEAR);
		int hour = 12;
		int mins = 00;
		Calendar beginTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();
		Intent event = new Intent();

		if (action.equalsIgnoreCase("insert")) {
			event.setAction(Intent.ACTION_INSERT)
			.setData(Events.CONTENT_URI);

			if (name != null) {
				event.putExtra(Events.TITLE, name);
			}
			if (location != null) {
				event.putExtra(Events.EVENT_LOCATION, location);
			}

			if (begin != null) {
				boolean stimeExists = false;
				if (begin.contains("am") || begin.contains("pm") || begin.contains("a.m") || begin.contains("p.m")) {
					stimeExists = true;
					format = new SimpleDateFormat("MMMM d h mm aa");
				} else {
					format = new SimpleDateFormat("MMMM d");
				}

				try {
					Date bDate = format.parse(begin);
					Calendar cal = Calendar.getInstance();
					cal.setTime(bDate);

					int month = cal.get(Calendar.MONTH);
					int day = cal.get(Calendar.DAY_OF_MONTH);

					if (stimeExists) {
						hour = cal.get(Calendar.HOUR_OF_DAY);
						mins = cal.get(Calendar.MINUTE);
					}
					beginTime.set(year, month, day, hour, mins);

				} catch (ParseException e) {
					e.printStackTrace();
				}

				event.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());
			}

			if (end != null) {
				etimeExists = false;
				if (end.contains("am") || end.contains("pm") || end.contains("a.m") || end.contains("p.m")) {
					etimeExists = true;
					format = new SimpleDateFormat("MMMM d h mm aa");
				} else {
					format = new SimpleDateFormat("MMMM d");
				}
				try {
					Date eDate = format.parse(end);
					Calendar cal = Calendar.getInstance();
					cal.setTime(eDate);

					int month = cal.get(Calendar.MONTH);
					int day = cal.get(Calendar.DAY_OF_MONTH);

					if (etimeExists) {
						hour = cal.get(Calendar.HOUR_OF_DAY);
						mins = cal.get(Calendar.MINUTE);
					}

					endTime.set(year, month, day, hour, mins);

				} catch (ParseException e) {
					e.printStackTrace();
				}
				event.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
			}
		}
		if (event.resolveActivity(manager) != null) {
			context.startActivity(event);
		} else {
			MainActivity.showMessage("Calendar commands not available on your device", (Activity)context);
		}


	}

	/**
	 * This is currently not being used, but could be implemented to help fix the Event commands
	 * @param date
	 * @return
	 */
	private String formatter(String date) {

		

			if (date.contains("am") || date.contains("pm") || date.contains("a.m") || date.contains("p.m")) {
				SimpleDateFormat format1 = new SimpleDateFormat("MMMM d 'at' h mm aa"); //September 4 at 3 15 pm
				SimpleDateFormat format2 = new SimpleDateFormat("MMM d 'at' h mm aa"); //Sept 4 at 3 15 pm
				SimpleDateFormat format3 = new SimpleDateFormat("MMM d'th' 'at' h mm aa"); //Sept 4th at 3 15 pm
				SimpleDateFormat format4 = new SimpleDateFormat("MMMM d'th' 'at' h mm aa"); //September 4th at 3 15 pm
				SimpleDateFormat format5 = new SimpleDateFormat("MMM d'st' 'at 'h mm aa"); //Sept 21st at 3 15 pm
				SimpleDateFormat format6 = new SimpleDateFormat("MMMM d'st' 'at' h mm aa"); //September 21st at 3 15 pm
				SimpleDateFormat format7 = new SimpleDateFormat("MMM d'nd' 'at' h mm aa"); //Sept 2nd at 3 15 pm
				SimpleDateFormat format8 = new SimpleDateFormat("MMMM d'nd' 'at' h mm aa"); //September 2nd at 3 15 pm
				SimpleDateFormat format9 = new SimpleDateFormat("MMM d'rd' 'at' h mm aa"); //Sept 3rd at 3 15 pm
				SimpleDateFormat format10 = new SimpleDateFormat("MMMM d'rd' 'at' h mm aa"); //September 3rd at 3 15 pm
				
				try {
					if (date.equals(format1.parse(date))) {
						
					}
					
				} catch (Exception e) {
					
				}
				

			} else {
				SimpleDateFormat format1 = new SimpleDateFormat("MMMM d"); //August 1
				SimpleDateFormat format2 = new SimpleDateFormat("MMM d"); //Aug 1
				SimpleDateFormat format3 = new SimpleDateFormat("MMMM d'th'"); //August 4th
				SimpleDateFormat format4 = new SimpleDateFormat("MMM d'th'"); //Aug 4th
				SimpleDateFormat format5 = new SimpleDateFormat("MMMM d'st'"); //August 1st
				SimpleDateFormat format6 = new SimpleDateFormat("MMM d'st'"); //Aug 1st
				SimpleDateFormat format7 = new SimpleDateFormat("MMMM d'rd'"); //August 3rd
				SimpleDateFormat format8 = new SimpleDateFormat("MMM d'rd'"); //Aug 3rd
				SimpleDateFormat format9 = new SimpleDateFormat("MMMM d'nd'"); //August 2nd
				SimpleDateFormat format10 = new SimpleDateFormat("MMM d'nd'"); //Aug 2nd
			}
			
			


		return null;
	}

	public void sms() {
		String action = (String) jsonObject.opt("action");
		String number = (String) jsonObject.opt("number");
		if (number != null) {
			number = number.replace("-", "");
		}
		String message = (String) jsonObject.opt("message");

		Intent sendSMS = new Intent();

		if (action.equalsIgnoreCase("send")) {
			sendSMS.setAction(Intent.ACTION_SENDTO);
			sendSMS.setData(Uri.parse("smsto:"));

			if (number != null) {
				sendSMS.putExtra("address", number);
			} 
			if (message != null) {
				sendSMS.putExtra("sms_body", message);
			}

		} else if (action.equalsIgnoreCase("open")) {
			sendSMS.setAction(Intent.ACTION_MAIN)
			.addCategory(Intent.CATEGORY_DEFAULT)
			.setType("vnd.android-dir/mms-sms");
		}
		if (sendSMS.resolveActivity(manager) != null) {
			context.startActivity(sendSMS);
		} else {
			MainActivity.showMessage("SMS commands not available on your device", (Activity)context);
		}

	}

	public void map() {
		String query = ((String) jsonObject.opt("query"));
		String dirFrom = (String) jsonObject.opt("directions-from");
		String dirTo = (String) jsonObject.opt("directions-to");
		String mode = (String) jsonObject.opt("mode");
		String avoid = (String) jsonObject.opt("avoid");

		Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"));
		mapIntent.setPackage("com.google.android.apps.maps");

		if (query != null) {
			query = Uri.encode(query);
			mapIntent.setData(Uri.parse("geo:0,0?q="+query));
		} 

		if (dirTo != null && dirFrom == null) {
			String directions = "google.navigation:q="+Uri.encode(dirTo);
			StringBuilder sb = new StringBuilder(directions); 

			if (mode != null) {
				if (mode.contains("driv")) 			sb.append("&mode=d");
				else if (mode.contains("walk")) 	sb.append("&mode=w");
				else if (mode.contains("bic") || mode.contains("bik")) sb.append("&mode=b");

			} else if (avoid != null) {
				sb.append("&avoid=");
				if (avoid.contains("toll")) sb.append("t");	
				if (avoid.contains("high")) sb.append("h");	
				if (avoid.contains("ferr")) sb.append("f");	
			} 
			directions = sb.toString();
			mapIntent.setData(Uri.parse(directions));
		} else if (dirTo != null && dirFrom != null) {
			mapIntent.setData(Uri.parse("http://maps.google.com/maps?saddr="+Uri.encode(dirFrom)+"&daddr="+Uri.encode(dirTo)));
		}
		if (mapIntent.resolveActivity(manager) != null) {
			context.startActivity(mapIntent);
		} else {
			MainActivity.showMessage("Google maps not available on your device", (Activity)context);
		}


	}

	public void camera() {
		String action = (String) jsonObject.opt("action");

		int requestCode=0;
		Intent camIntent = new Intent();

		if (action.equalsIgnoreCase("photo") || action.equalsIgnoreCase("selfie")) {
			camIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			requestCode = 2;
			if (action.equalsIgnoreCase("selfie")) {
				camIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
			}

		} else if (action.equalsIgnoreCase("video")) {
			camIntent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
			requestCode = 4;
		}
		if (camIntent.resolveActivity(manager) != null){
			((Activity)context).startActivityForResult(camIntent, requestCode);
		} else {
			MainActivity.showMessage("Camera commands not available on your device", (Activity)context);
		}
	}

	public void alarm() { 
		String action = (String) jsonObject.opt("action");
		String hour = (String) jsonObject.opt("hour");
		String mins = (String) jsonObject.opt("minutes");
		String ampm = (String) jsonObject.opt("ampm");
		String name = (String) jsonObject.opt("name");
		String day = (String) jsonObject.opt("day");

		Intent alarm = new Intent();

		if (action.equalsIgnoreCase("alarm")) {
			alarm.setAction(AlarmClock.ACTION_SET_ALARM);

			if (hour != null) {
				int intHour = Integer.parseInt(hour);


				if (ampm != null) {
					if (ampm.equalsIgnoreCase("p.m") || ampm.equalsIgnoreCase("pm")) {
						if (intHour != 12) {
							intHour += 12;
						}
					} else if (ampm.equalsIgnoreCase("a.m") || ampm.equalsIgnoreCase("am")) {
						if (intHour == 12) {
							intHour = 0;
						}
					}
				}
				alarm.putExtra(AlarmClock.EXTRA_HOUR, intHour);
			}

			if (mins != null) {
				alarm.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(mins));
			} else {
				alarm.putExtra(AlarmClock.EXTRA_MINUTES, 00);
			}

			if (name != null) {
				alarm.putExtra(AlarmClock.EXTRA_MESSAGE, name);
			}

			if (day != null) {
				ArrayList<Integer> alarmDays = new ArrayList<Integer>();

				if (day.toLowerCase().contains("sun")) 	alarmDays.add(1);				 
				if (day.toLowerCase().contains("mon")) 	alarmDays.add(2);
				if (day.toLowerCase().contains("tue")) 	alarmDays.add(3);
				if (day.toLowerCase().contains("wed")) 	alarmDays.add(4);				
				if (day.toLowerCase().contains("thur")) alarmDays.add(5);
				if (day.toLowerCase().contains("fri"))	alarmDays.add(6);
				if (day.toLowerCase().contains("sat"))	alarmDays.add(7);

				alarm.putExtra(AlarmClock.EXTRA_DAYS, alarmDays);
			}
		} else if (action.equalsIgnoreCase("timer")) {
			alarm.setAction(AlarmClock.ACTION_SET_TIMER);
			int seconds=0;

			if (mins != null || hour != null) {
				if (mins != null)
					seconds += (Integer.parseInt(mins) * 60);

				if (hour != null) {
					seconds += (Integer.parseInt(hour) * 3600);
				}
				alarm.putExtra(AlarmClock.EXTRA_LENGTH, seconds);
			}

			if (name != null) {
				alarm.putExtra(AlarmClock.EXTRA_MESSAGE, name);
			}



		} else if (action.equalsIgnoreCase("show")) {
			alarm.setAction(AlarmClock.ACTION_SHOW_ALARMS);
		}
		if (alarm.resolveActivity(manager) != null) {
			context.startActivity(alarm);
		} else {
			MainActivity.showMessage("Alarm commands not available on your device", (Activity)context);
		}

	}

	public void phone() {
		String action = (String) jsonObject.opt("action");
		String number = (String) jsonObject.opt("number");

		Intent phoneCall = new Intent();

		if (action.equalsIgnoreCase("call")) {
			phoneCall.setAction(Intent.ACTION_CALL);

		} else if (action.equalsIgnoreCase("dial")) {
			phoneCall.setAction(Intent.ACTION_DIAL);
		} 

		if (number != null) {
			number.replace("-", "");
			number.replace(" ", "");

			phoneCall.setData(Uri.parse("tel:"+number));
		} else {
			phoneCall.setData(Uri.parse("tel:"));
		}
		if (phoneCall.resolveActivity(manager) != null) {
			context.startActivity(phoneCall);
		} else {
			MainActivity.showMessage("Phone commands not available on your device", (Activity)context);
		}

	}

	public void google() {
		String query = ((String) jsonObject.opt("query")).replace(" ", "%20");

		Intent google = new Intent(Intent.ACTION_VIEW);
		if (query != null) {
			google.setData(Uri.parse("https://www.google.com/search?q="+query));
		}
		if (google.resolveActivity(manager) != null) {
			context.startActivity(google);
		} else {
			MainActivity.showMessage("Google not available on your device", (Activity)context);
		}

	}




}

