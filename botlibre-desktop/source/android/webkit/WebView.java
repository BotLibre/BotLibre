/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package android.webkit;

import javax.swing.JComponent;

import android.widget.TextView;

/**
 * Stub class.
 */
public class WebView extends TextView {
	public WebViewClient client;

	public WebView(JComponent component) {
		super(component);
	}
	
	public void loadDataWithBaseURL(String url, String data, String mime, String ecoding, String history) {
		if (!data.contains("<html")) {
			setText("<html>" + data + "</html>");
		} else {
			setText(data);
		}
	}
	
	public void setWebViewClient(WebViewClient client) {
		this.client = client;
	}
	
	public WebSettings getSettings() {
		return new WebSettings();
	}
}