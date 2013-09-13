package com.slideshow.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 */
public class Slideshow_Configuration implements EntryPoint {

	
	/**
	 * This is the entry point method. For UI
	 */
	public void onModuleLoad() {
		FlexTable flexTable = new FlexTable();
		flexTable.setText(0, 0, "Setting");
		flexTable.setText(0, 1, "Time");
		flexTable.setText(0, 2, "Format");
		flexTable.setCellPadding(6);
		flexTable.getRowFormatter().addStyleName(0, "columnHeader");
		flexTable.getColumnFormatter().addStyleName(1, "column");
		flexTable.getColumnFormatter().addStyleName(0, "column");
		flexTable.getColumnFormatter().addStyleName(2, "column");
		flexTable.addStyleName("flexTable");
		
		final FormPanel form = new FormPanel();
		//form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);
		
		VerticalPanel holder = new VerticalPanel();
		holder.add(flexTable);
		holder.addStyleName("table");
		// First row elements
		Label durationLable = new Label("Duration");
		
		ListBox durationTime = new ListBox();
		for(int i =1; i<=60;i++){
			durationTime.addItem(i+"");
		}
		durationTime.setName("durationTime");
		
		ListBox durationFormat = new ListBox();
		durationFormat.addItem("seconds");
		durationFormat.addItem("minutes");
		durationFormat.setName("durationTimeFormat");


		flexTable.setWidget(1, 0, durationLable);
		flexTable.setWidget(1, 1, durationTime);
		flexTable.setWidget(1, 2, durationFormat);
		
		// Second row elements
		Label presetLabel = new Label("Preset (1 cycle)");
		ListBox presetTime = new ListBox();
		presetTime.setName("presetTime");
		for(int i =1; i<=60;i++){
			presetTime.addItem(i+"");
		}
		ListBox presetFormat = new ListBox();
		presetFormat.addItem("seconds");
		presetFormat.addItem("minutes");
		presetFormat.setName("presetTimeFormat");
		
		flexTable.setWidget(2, 0, presetLabel);
		flexTable.setWidget(2, 1, presetTime);
		flexTable.setWidget(2, 2, presetFormat);

		Button configureButton = new Button("Configure on all devices", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				form.submit();
				
			}
		});
		configureButton.addStyleName("configureButton");
		holder.add(configureButton);
		
		form.add(holder);
		form.setAction("storeSettings");

		RootPanel.get("configuration").add(form);
		RootPanel.get("configuration").addStyleName("container");
		
	}
}
