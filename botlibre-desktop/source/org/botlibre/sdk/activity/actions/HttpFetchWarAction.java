///******************************************************************************
// *
// *  Copyright 2014 Paphus Solutions Inc.
// *
// *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *      http://www.eclipse.org/legal/epl-v10.html
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// *
// ******************************************************************************/
//
//package org.botlibre.sdk.activity.actions;
//
//import org.botlibre.sdk.activity.war.StartWarActivity;
//import org.botlibre.sdk.config.InstanceConfig;
//import org.botlibre.sdk.config.WebMediumConfig;
//
//import android.app.Activity;
//
//public class HttpFetchWarAction extends HttpFetchAction {
//
//	public HttpFetchWarAction(Activity activity, WebMediumConfig config) {
//		super(activity, config);
//	}
//
//	@Override
//	public void onPostExecute(String xml) {
//		super.superOnPostExecute(xml);
//		StartWarActivity.bot1 = (InstanceConfig)this.config;
//		((StartWarActivity)this.activity).resetView();
//	}
//	
//}