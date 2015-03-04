package org.melophonic.audio.abx.ui;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;

import org.controlsfx.control.RangeSlider;
import org.eclipse.fx.ui.dialogs.MessageDialog;
import org.melophonic.audio.abx.AbxApp;
import org.melophonic.audio.abx.api.AbxMedia;
import org.melophonic.audio.abx.api.AbxMode;
import org.melophonic.audio.abx.api.AbxTest;
import org.melophonic.audio.abx.api.AbxTest.Result;
import org.melophonic.audio.abx.api.AbxTest.Trial;
import org.melophonic.audio.abx.api.ProgressHandler;
import org.melophonic.audio.abx.impl.AbxFileImpl;
import org.melophonic.audio.abx.impl.AbxFileSetImpl;
import org.melophonic.audio.abx.impl.AbxMediaFactoryImpl;
import org.melophonic.audio.abx.impl.AbxMediaSetImpl;
import org.melophonic.audio.abx.impl.AbxMediaSetImpl.AbxMediaFile;
import org.melophonic.audio.spi.AnalysisService;
import org.melophonic.audio.spi.FingerprintService;
import org.melophonic.audio.spi.musicg.MGFingerprintService;
import org.melophonic.audio.spi.tarsos.TarsosAnalysisService;
import org.slf4j.LoggerFactory;

public class AbxOverviewController implements Initializable, ListChangeListener<AbxMediaFile>, ProgressHandler, ChangeListener<AbxMediaFile> {

	static {
		// TODO: shirley, there's a better way
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");
	}

	final static org.slf4j.Logger log = LoggerFactory.getLogger(AbxOverviewController.class);
	final static int DEFAULT_TRIALS_PER_FILE = 3;
	final static int MAX_TRIALS = 99;
	
	final static String[] DEMO_HI_RES = {"naim-test-1-wav-16-44100.wav", "naim-test-1-wav-24-96000.wav"};
	final static String[] DEMO_GAIN_OFFSET = {"naim-test-1-wav-16-44100.wav", "naim-test-1-wav-16-44100-cropped.wav", "naim-test-1-wav-16-44100-low-gain.wav"};
	
	@FXML
	private BorderPane decorationArea;

	@FXML
	private TableView<AbxMediaFile> audioFileTable;
	@FXML
	private TableColumn<AbxMediaFile, String> idColumn;
	@FXML
	private TableColumn<AbxMediaFile, Boolean> enabledColumn;
	@FXML
	private TableColumn<AbxMediaFile, File> fileColumn;
	@FXML
	private TableColumn<AbxMediaFile, Duration> offsetColumn;
	@FXML
	private TableColumn<AbxMediaFile, Number> gainColumn;
	@FXML
	private TableColumn<AbxMediaFile, Duration> durationColumn;
	
	
	@FXML
	private BorderPane mediaPane;
	@FXML
	private Label statusLabel;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private MenuItem calculateOffsetsMenuItem;
	@FXML
	private MenuItem calculateGainMenuItem;
	@FXML
	private Button removeButton;

	
	@FXML
	private Button playButton;
	@FXML
	private Button stopButton;
	@FXML
	private CheckBox loopCheckbox;
	@FXML
	private Label timeLabel;
	@FXML
	private Slider timeSlider;
	@FXML
	private Slider volumeSlider;
	
	@FXML
	private FlowPane sliderPane;
	
	private RangeSlider rangeSlider;
	
	
	@FXML
	private Label startTimeLabel;
	@FXML
	private Label endTimeLabel;
	
	@FXML
	private Button testButton;
	@FXML 
	private ChoiceBox<AbxMode> modeChoiceBox;
	@FXML
	private TextField trialsField;
	@FXML
	private BorderPane testPane;
	@FXML
	private HBox testButtonBox;
	@FXML
	private TextFlow testResults;
	
	//private MediaView mediaView;
	private AbxApp<AbxMediaSetImpl.AbxMediaFile, AbxMediaSetImpl> app;
	
	boolean isTest = false;
	private TestModel<?> testModel;
	
	private Image playImage, pauseImage, stopImage, restartImage;
	
	public AbxOverviewController() {
		super();

	}

	public void initialize(URL location, ResourceBundle resources) {
		log.info("begin");
		
		fileColumn.setCellFactory(column -> {
			return new TableCell<AbxMediaFile, File>() {
				@Override
				protected void updateItem(File item, boolean empty) {
					super.updateItem(item, empty);
					setText(item == null ? "" : (item.getName() + " "));
				}
			};
		});

		idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		enabledColumn.setCellValueFactory(cellData -> cellData.getValue().enabledProperty());
		fileColumn.setCellValueFactory(cellData -> cellData.getValue().fileProperty());
		gainColumn.setCellValueFactory(cellData -> cellData.getValue().gainProperty());
		offsetColumn.setCellValueFactory(cellData -> cellData.getValue().offsetProperty());
		durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
		

		gainColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
		gainColumn.setOnEditCommit(new EventHandler<CellEditEvent<AbxMediaFile, Number>>() {
			@Override
			public void handle(CellEditEvent<AbxMediaFile, Number> t) {
				double newValue = ((Number) t.getNewValue()).doubleValue();
				/*
				 * ((ABXAudioFileManager) t.getTableView().getItems()
				 * .get(t.getTablePosition().getRow())) .setGain(newValue);
				 */
				t.getRowValue().getMedia().setVolume(newValue);
				//mediaControl.updateValues();
				log.info("TODO: updateValues()");
			}
		});

		offsetColumn.setCellFactory(cellData -> new DurationCell());
		offsetColumn.setOnEditCommit(new EventHandler<CellEditEvent<AbxMediaFile, Duration>>() {
			@Override
			public void handle(CellEditEvent<AbxMediaFile, Duration> t) {
				t.getRowValue().setOffset(t.getNewValue());
			}
		});

		enabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(enabledColumn));

		
		durationColumn.setCellFactory(cellData -> new DurationCell());
		

		playImage = new Image(getClass().getResource("/play.png").toString());
		stopImage = new Image(getClass().getResource("/stop.png").toString());
		pauseImage = new Image(getClass().getResource("/pause.png").toString());
		restartImage = new Image(getClass().getResource("/restart.png").toString());
		
		setImage(playButton, playImage);
		setImage(stopButton, stopImage);
		
		timeSlider.valueProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				if (timeSlider.isValueChanging()) {
					// multiply duration by percentage calculated by slider
					// position
					getCurrentFile().getMedia().seek(getDurationFromSliderValue(timeSlider.getValue(), getCurrentFile().getDuration()));
					updateValues();
				}
			}
		});		
		
		volumeSlider.valueProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				if (volumeSlider.isValueChanging()) {
					double newGain = volumeSlider.getValue() / 100.0;
					//gainChanging = true;
					getCurrentMedia().setVolume(newGain);
					getCurrentFile().setGain(newGain);
					//gainChanging = false;
				}
			}
		});
		
		
		rangeSlider = new RangeSlider(0, 100, 0, 100);
		rangeSlider.setPrefWidth(375);
	
		rangeSlider.highValueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (rangeSlider.isHighValueChanging()) {
					app.getMediaSet().setEndTime(getDurationFromSliderValue(newValue.doubleValue(), getShortestFile(true).getDuration()));
				}
			}
		});
		
		rangeSlider.lowValueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (rangeSlider.isLowValueChanging()) {
					app.getMediaSet().setStartTime(getDurationFromSliderValue(newValue.doubleValue(), getShortestFile(true).getDuration()));
				}
			}
		});		

		
		sliderPane.getChildren().add(rangeSlider);
		
		modeChoiceBox.getItems().addAll(AbxMode.values());
		modeChoiceBox.getSelectionModel().selectFirst();
		
		trialsField.textProperty().addListener(new ChangeListener<String>() {

	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	        	int minValue = 1;
	        	int maxValue = MAX_TRIALS;
	            try {
	            	int intValue = Integer.parseInt(newValue.trim());
	                if (intValue < minValue || intValue > maxValue) throw new Exception();
	            	trialsField.setText(newValue);
	            } catch (Exception e) {
	            	handleError("Trials", String.format("Must be an integer between %s and %s", minValue, maxValue));
	            }
	        }
		});
		
		
		//mediaView = new MediaView();
		
		updateEnabledState();

		try {
			FingerprintService fingerprintService = new MGFingerprintService();
			AnalysisService analysisService = new TarsosAnalysisService();
			AbxMediaSetImpl manager = new AbxMediaSetImpl(new AbxMediaFactoryImpl(), audioFileTable.getItems());
			this.app = new AbxApp<>(manager, fingerprintService, analysisService);
		} catch (Exception e) {
			handleError("init", e);
		}
		
		app.getMediaSet().currentFileProperty().addListener(this);
		/*
		manager.currentFileProperty().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				log.info("invalidated {}", manager.getCurrentFile());
				
			}
		});
		*/
		/*
		app.getMediaSet().clipProperty().addListener(new ChangeListener<AbxClipImpl>() {

			@Override
			public void changed(ObservableValue<? extends AbxClipImpl> observable, AbxClipImpl oldValue, AbxClipImpl newValue) {
				update(newValue.getStartTime(), newValue.getEndTime());
				
				newValue.startTimeProperty().addListener(new ChangeListener<Duration>() {
					@Override
					public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
						update(newValue, null);
					}
				});

				newValue.endTimeProperty().addListener(new ChangeListener<Duration>() {
					@Override
					public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
						update(null, newValue);
					}
				});		
				
			}
			
			
			
		});
		*/
		app.getMediaSet().startTimeProperty().addListener(new ChangeListener<Duration>() {
			@Override
			public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
				update(newValue, null);
			}
		});

		app.getMediaSet().endTimeProperty().addListener(new ChangeListener<Duration>() {
			@Override
			public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
				update(null, newValue);
			}
		});		


		audioFileTable.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
			int selectedIndex = audioFileTable.getSelectionModel().getSelectedIndex();
			setCurrentFile(selectedIndex >= 0 && selectedIndex < getFiles().size() ? getFiles().get(selectedIndex) : null);
		});
		
		audioFileTable.getItems().addListener(this);


		newFileSet();

		log.info("complete");

		
	
	
	}
	
	/** delegate methods **/
	
	public AbxMediaFile getCurrentFile() {
		return app.getMediaSet().getCurrentFile();
	}
	
	public void setCurrentFile(AbxMediaFile abxFile) {
		app.getMediaSet().setCurrentFile(abxFile);
	}

	public List<AbxMediaFile> getFiles() {
		return app.getMediaSet().getFiles();
	}

	public List<AbxMediaFile> getEnabledFiles() {
		return app.getMediaSet().getEnabledFiles();
	}
	
	public AbxMedia getCurrentMedia() {
		return getCurrentFile() != null ? getCurrentFile().getMedia() : null;	
	}

	public AbxMediaFile getShortestFile(boolean recalculate) {
		return app.getMediaSet().getShortestFile(true);
	}
	

	
	/** bindings **/
	
	@FXML
	public void openHiResDemo() {
		StringBuffer message = new StringBuffer("Contains two different versions of the same audio at different resolutions.\n");
		message.append("Use the test feature to see if you can hear a difference (headphones recommended).");
		openDemo("High-Res Audio Demo", message.toString(), DEMO_HI_RES);
	}
	
	@FXML
	public void openGainOffsetDemo() {
		StringBuffer message = new StringBuffer("Contains three different versions of the same file: normal, cropped, and reduced-gain.\n");
		message.append("Use the options under the Edit menu to automatically align and adjust gain & timing.");
		openDemo("Gain/Offset Demo", message.toString(), DEMO_GAIN_OFFSET);
	}
	
	public void openDemo(String name, String message, String...resourceNames) {
		boolean showMessage = true;
		try {
			app.load(URLEncoder.encode(name, "UTF8"), AbxApp.toUrls(getClass(), "/", resourceNames));	
		} catch (Exception e) {
			showMessage = false;
			handleError(name, e);
		} finally {
			if (showMessage) MessageDialog.openInformationDialog(getWindow(), name, message);
		}
	}

	
	@FXML
	public void newFileSet() {
		openFileSet(new AbxFileSetImpl<AbxFileImpl>());
	}

	@FXML
	public void openFileSet() {
		try {
			final FileChooser fileChooser = new FileChooser();
			File file = fileChooser.showOpenDialog(getWindow());
			if (file != null) {
				openFileSet(AbxFileSetImpl.unmarshal(file));
			}			
		} catch (Exception e) {
			handleError("opening file", e);
		}
	}

	@FXML
	public void saveFileSet() {
		try {
			if (app.getMediaSet().getSerializedFile() == null) {
				final FileChooser fileChooser = new FileChooser();
				File file = fileChooser.showSaveDialog(getWindow());
				if (file != null) {
					app.getMediaSet().save(file);
				}
			}
		} catch (Exception e) {
			handleError("saving file", e);
		}
	}
	

	public void openFileSet(AbxFileSetImpl<AbxFileImpl> fileSet) {
		try {
			app.getMediaSet().load(fileSet);
			
		} catch (Exception e) {
			handleError("openFileSet", e);
		}
	}	
	
	TableView<AbxMediaFile> getAudioFileTable() {
		return audioFileTable;
	}

	Window getWindow() {
		return decorationArea.getScene().getWindow();
	}

	public void handleError(String title, String message) {
		MessageDialog.openErrorDialog(getWindow(), title, message);
	}

	public void handleError(String title, Throwable x) {
		MessageDialog.openErrorDialog(getWindow(), title, x.getClass().getSimpleName() + ": " + x.getMessage());
		x.printStackTrace();
	}
	
	void setImage(Button button, Image image) {
		button.setGraphic(new ImageView(image));
	}

	void update(Duration startTime, Duration endTime) {
		Duration maxDuration = null;
		AbxMediaFile shortest = getShortestFile(true);
		if (shortest != null) maxDuration = shortest.getDuration();
		
		if (startTime != null) {
			if (!rangeSlider.isLowValueChanging()) {
				if (startTime.equals(Duration.ZERO)) rangeSlider.setLowValue(0.0);
				else if (maxDuration != null) rangeSlider.setLowValue(toSliderValue(startTime, maxDuration));
				else log.error("Unable to set rangerSlider low value {}", startTime);
			}
			updateLabel(startTimeLabel, startTime);
		}

		if (endTime != null) {
			boolean isIndefinite = endTime.equals(Duration.INDEFINITE);
			Duration endTimeCalc = isIndefinite && shortest != null ? maxDuration : endTime;
			if (!rangeSlider.isHighValueChanging()) {
				if (endTimeCalc == null || maxDuration == null) log.error("Unable to set rangerSlider high value {}", endTime);
				else rangeSlider.setHighValue(toSliderValue(endTimeCalc, maxDuration));
			}
			if (endTimeCalc != null) updateLabel(endTimeLabel, endTimeCalc.isIndefinite() ? Duration.ZERO : endTimeCalc);
		}
		
	}
				
	void updateLabel(Label label, Duration newValue) {
		label.setText(formatTime(newValue));
	}
	
	
	
	
	@Override
	public void changed(ObservableValue<? extends AbxMediaFile> observable, AbxMediaFile currentFile, AbxMediaFile selectedFile) {
		log.info("currentFile changed from {} to {} as test {}", currentFile, selectedFile, isTest);
		
		if (currentFile != null) {
			if (currentFile.equals(selectedFile) && !isTest) return; // file is not changing
			if (currentFile.getMedia().isPlaying()) {
				currentFile.getMedia().pause();
			}
		}
		
		if (selectedFile != null) {
			
			//mediaView.setMediaPlayer(selectedFile.getPlayer());
			selectedFile.getMedia().activate();
			updateValues();
			
			if (isTest) {
				statusLabel.setText("");
				testModel.setEnabled(true);
			} else {
				statusLabel.setText(String.format("%s: %s", selectedFile.getMedia().getFormat().toString(), selectedFile.getDuration()));	
			}
			
		} 
		updateEnabledState();
	}
	
	abstract class TestModel<R> {
		
		final AbxTest<AbxMediaFile> test = app.createTest(modeChoiceBox.getValue(), Integer.parseInt(trialsField.getText()));;
		final Iterator<Trial<AbxMediaFile, R>> trialIterator = test.iterator();
		final Label statusLabel = new Label("");
		final List<Button> buttons = new ArrayList<Button>();
		
		Trial<AbxMediaFile, R> currentTrial;
		int trialNumber = 0;
		
		boolean nextTrial() {
			
			boolean hasNext = false;
			if (trialIterator.hasNext()) {
				currentTrial = trialIterator.next();
				trialNumber++;
				setEnabled(false);
				log.info("nextTrial {}", currentTrial.getFile());
				app.getMediaSet().setCurrentFile(null); // TODO: hack to force change event
				app.getMediaSet().setCurrentFile(currentTrial.getFile());
				
				hasNext = true;
			}
			statusLabel.setText(getStatus(!hasNext));
			return hasNext;
		}
		
		public void setEnabled(boolean enabled) {
			for (Button button : buttons) button.setDisable(!enabled);
		}
		
		public List<Button> getButtons() {
			if (buttons.isEmpty()) buttons.addAll(createButtons());
			return buttons;
		}
		
		abstract String getStatus(boolean isComplete);
		abstract List<Button> createButtons();
		abstract List<Text> getResults();
	}
	
	class ABXTestModel extends TestModel<AbxMediaFile> {
		
		String getStatus(boolean isComplete) {
			if (isComplete) {
				return String.format("Test complete! Correctly identified file %s out of %s times (accuracy: %.0f%%)\n\n", test.getMatches(), test.size(), test.getAccuracy() * 100);
			} else {
				return String.format("Test %s of %s. Listen and then choose or replay", trialNumber, test.size());
			}
		}


		@Override
		List<Button> createButtons() {
			List<Button> buttons = new ArrayList<Button>();
			for (final AbxMediaFile abxFile : getEnabledFiles()) {
				Button abxFileButton = new Button(abxFile.getId());
				abxFileButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						currentTrial.setResponse(abxFile);
						performTrial();
					}
				});
				buttons.add(abxFileButton);
			}			
			return buttons;
		}

		@Override
		List<Text> getResults() {
			List<Text> results = new ArrayList<Text>();
			int trialNo = 1;
			Iterator<Trial<AbxMediaFile, AbxMediaFile>> it = test.iterator();
			while (it.hasNext()) {
				Trial<AbxMediaFile, AbxMediaFile> trial = it.next();
			     Text text1 = new Text(String.format("Trial %s: Choice/Actual: %s/%s\n", trialNo++, trial.getResponse().getId(), trial.getFile().getId()));
			     if (test.isMatch(trial) == false) text1.setFill(Color.RED);
			     results.add(text1);
			}			
			return results;
		}
		
	}

	class ShootoutTestModel extends TestModel<Boolean> {
		
		String getStatus(boolean isComplete) {
			if (isComplete) {
				return String.format("Test complete! Preferences by file listed below");
			} else {
				return String.format("Test %s of %s. Listen and then choose a preference, or replay", trialNumber, test.size());
			}
		}

		Button createButton(final String label, final Boolean resultValue) {
			Button button = new Button(label);
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					currentTrial.setResponse(resultValue);
					performTrial();
				}
			});		
			return button;
		}
		

		@Override
		List<Button> createButtons() {
			List<Button> buttons = new ArrayList<Button>();
			buttons.add(createButton("Positive", true));	
			buttons.add(createButton("Negative", false));	
			return buttons;
		}

		@Override
		List<Text> getResults() {
			List<Text> results = new ArrayList<Text>();
			Map<AbxMediaFile, Result> resultsMap = test.getResultsByFile();
			for (AbxMediaFile file : resultsMap.keySet()) {
				Result result = resultsMap.get(file);
			     Text text1 = new Text(String.format("%s: %s: (%s/%s) %.0f%% positive\n", file.getId(), file, result.hits(), result.trials(), result.score()));
			     results.add(text1);
			}			
			return results;
		}
		
	}
	
	
	
	TestModel<?> newTestModel(AbxMode mode) {
		switch (mode) {
			case Shootout: return new ShootoutTestModel();
			default: return new ABXTestModel();
		}
	}
	
	
	@FXML
	public void handleTest() {
		if (isTest) {	
			testButton.setText("Test");
			this.isTest = false;
		} else {
			testButton.setText("Cancel");
			this.isTest = true;
			statusLabel.setText("");
			this.testModel = newTestModel(modeChoiceBox.getValue());
			
			testResults.getChildren().clear();
			testButtonBox.getChildren().clear();
			
			
			for (Button button : testModel.getButtons()) {
				testButtonBox.getChildren().add(button);
			}
			
			testButtonBox.getChildren().add(testModel.statusLabel);
			
			performTrial();
		}
		updateEnabledState();
		
	}	
	

	void performTrial() {
		if (getCurrentMedia() != null && getCurrentMedia().isPlaying()) {
			getCurrentMedia().stop();
		}
		
		testResults.getChildren().clear();
		if (testModel.nextTrial()) {
			getCurrentMedia().play();
		} else {
			for (Node n : testButtonBox.getChildren()) n.setDisable(true);
			for (Text result : testModel.getResults()) testResults.getChildren().add(result);
			handleTest();
		}
	}	
	
	

	@FXML
	public void handleRemoveAudioFile() {
		if (getCurrentFile() != null) {
			app.getMediaSet().removeFile(getCurrentFile());
		} else {
			MessageDialog.openWarningDialog(getWindow(), "No file selected", "Select a file to remove");
		}
	}

	@FXML
	public void handlePlayAudioFile() {
		if (getCurrentFile() != null) {
			if (getCurrentFile().getMedia().isPlaying()) getCurrentFile().getMedia().pause();
			else getCurrentFile().getMedia().play();
		} else {
			MessageDialog.openWarningDialog(getWindow(), "No file selected", "Select a file to play");
		}
	}

	@FXML
	public void handleAddAudioFile() {
		final FileChooser fileChooser = new FileChooser();
		File file = fileChooser.showOpenDialog(getWindow());
		if (file != null) {
			try {
				app.getMediaSet().addFile(file);
			} catch (Exception e) {
				MessageDialog.openErrorDialog(getWindow(), "Error adding file", e.toString());
			}
		}
	}

	@FXML
	public void handleCalculateOffsets() {
		log.info("begin");

		calculateOffsetsMenuItem.setDisable(true);
		progressBar.setProgress(0.0);
		progressBar.setVisible(true);
		getWindow().getScene().setCursor(Cursor.WAIT);
		
		app.createCalculateOffsetTaskRunner().start(this, getEndRunner(calculateOffsetsMenuItem));
	}
	
	Runnable getEndRunner(final MenuItem menuItem) {
		return new Runnable() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						getWindow().getScene().setCursor(Cursor.DEFAULT);
						menuItem.setDisable(false);
						setProgress(1.0, false);
					}
				});
				
			}
		};
	}
	
	
	@FXML
	public void handleCalculateGain() {
		log.info("begin");

		calculateGainMenuItem.setDisable(true);
		progressBar.setProgress(0.0);
		progressBar.setVisible(true);
		getWindow().getScene().setCursor(Cursor.WAIT);
		
		app.createCalculateGainTaskRunner().start(this, getEndRunner(calculateGainMenuItem));


	}	

	


	


	public void setProgress(double progress, boolean increment) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (progress == 1.0) {
					progressBar.setProgress(0.0);
					getWindow().getScene().setCursor(Cursor.DEFAULT);
				} else {
					progressBar.setProgress(increment ? progressBar.getProgress() + progress : progress);
				}
			}
		});
	}

	@FXML
	public void handleClose() {
		log.info("handleClose");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Platform.exit();
			}
		});

	}

	@FXML
	public void handlePlayPause() {
		AbxMedia media = getCurrentMedia();

		if (!media.isAccessible()) {
			// don't do anything in these states
			return;
		}

		if (media.isReady()) {
			// rewind the movie if we're sitting at the end
			/*
			if (atEndOfMedia) {
				log.info("rewind");
				getMediaPlayer().seek(getMediaPlayer().getStartTime());
				atEndOfMedia = false;
			}
			*/
			getCurrentMedia().play();
		} else {
			getCurrentMedia().pause();
		}

	}
	
	@FXML
	public void handleStop(ActionEvent e) {
		if (getCurrentMedia() != null) getCurrentMedia().stop();
	}



	

	public void updateEnabledState() {
		int enabledFiles = app != null ? getEnabledFiles().size() : 0;
		
		boolean currentFileOperationsDisabled = app == null || getCurrentFile() == null;
		boolean multiFileOperationsDisabled = isTest || app == null || getFiles().size() < 2;
		boolean fileSetOperationsDisabled = isTest || audioFileTable.getItems().isEmpty();
		boolean testOperationsDisabled = enabledFiles < 2;
		
		playButton.setDisable(currentFileOperationsDisabled);
		removeButton.setDisable(isTest || currentFileOperationsDisabled);
		timeSlider.setDisable(isTest || currentFileOperationsDisabled);
		volumeSlider.setDisable(isTest || currentFileOperationsDisabled);	
		
		calculateOffsetsMenuItem.setDisable(multiFileOperationsDisabled);
		calculateGainMenuItem.setDisable(multiFileOperationsDisabled);
		
		testButton.setDisable(testOperationsDisabled);
		modeChoiceBox.setDisable(isTest || testOperationsDisabled);
		trialsField.setDisable(isTest || testOperationsDisabled);
		if (!testOperationsDisabled) {
			trialsField.setText(String.valueOf(DEFAULT_TRIALS_PER_FILE * enabledFiles));
			
		}
		
		rangeSlider.setDisable(fileSetOperationsDisabled);
		
		updateValues();
	}

	public void updateValues() {
	
		if (app != null && getCurrentFile() != null) {
			//log.info("updateValues: " + manager.getCurrentFile().getPlayer().se);
			
			Platform.runLater(new Runnable() {
				public void run() {
					if (getCurrentMedia() == null) return;
					if (isTest) return;
					
					Duration currentTime = getCurrentMedia().getCurrentTime();
					Duration duration = getCurrentFile().getDuration();
					timeLabel.setText(formatTime(currentTime, duration));
					timeSlider.setDisable(duration == null || duration.isUnknown());
					
					if (!timeSlider.isValueChanging()) {					
						timeSlider.setValue(duration.greaterThan(Duration.ZERO) ? toSliderValue(currentTime, duration) : 0);
					}

					if (!volumeSlider.isValueChanging()) {
						volumeSlider.setValue((int) Math.round(getCurrentMedia().getVolume() * 100));
					}
				}
			});
			
		}		
		
	}
	
	@Override
	public void onChanged(Change<? extends AbxMediaFile> c) {
		while (c.next()) {
			if (c.wasAdded()) {
				filesAdded(c.getAddedSubList());
			}
		}
		
		statusLabel.setText("");
		audioFileTable.getSelectionModel().clearAndSelect(0);
	}
	
	public void filesAdded(List<? extends AbxMediaFile> abxFiles) {
		for (AbxMediaFile abxFile : abxFiles) {
			
			try {
				//abxFile.initialize(manager.getClip());
				
				abxFile.enabledProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
						updateEnabledState();
						
					}
				});
				
				AbxMedia media = abxFile.getMedia();
				/*
				mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
					public void invalidated(Observable ov) {
						log.debug("invalidated");
						updateValues();
					}
				});
				*/
				
				media.currentTimeProperty().addListener(new ChangeListener<Duration>() {

					@Override
					public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
						Platform.runLater(new Runnable() {
							public void run() {
								if (!isTest) {
									Duration currentTime = getCurrentMedia().getCurrentTime();
									Duration duration = getCurrentFile().getDuration();
									timeLabel.setText(formatTime(currentTime, duration));
									timeSlider.setDisable(duration == null || duration.isUnknown());
									
									if (!timeSlider.isValueChanging() || rangeSlider.isHighValueChanging() || rangeSlider.isLowValueChanging()) {					
										timeSlider.setValue(duration.greaterThan(Duration.ZERO) ? toSliderValue(currentTime, duration) : 0);
									}
								}
								
							}
						});
						
					}
					
				});

				media.setOnPlaying(new Runnable() {
					public void run() {
						log.debug("onPlaying");
						/*
						if (stopRequested) {
							mediaPlayer.pause();
							stopRequested = false;
						} else {
							setImage(playButton, pauseImage);
						}
						*/
						setImage(playButton, pauseImage);
						stopButton.setDisable(false);
						updateValues();
					}
				});

				media.setOnPaused(new Runnable() {
					public void run() {
						log.debug("onPaused");
						setImage(playButton, playImage);
					}
				});

				media.setOnStopped(new Runnable() {
					public void run() {
						log.debug("onStopped");
						stopButton.setDisable(true);
						setImage(playButton, playImage);
					}
				});

				media.setOnReady(new Runnable() {
					public void run() {
						log.debug("onReady");
						//duration = mediaPlayer.getMedia().getDuration();
						updateValues();
					}
				});

				
				media.setOnEndOfMedia(new Runnable() {
					public void run() {
						log.debug("onEndOfMedia");
						if (!loopCheckbox.isSelected()) {
							setImage(playButton, playImage);
							
							//stopRequested = true;
							//atEndOfMedia = true;
							
							getCurrentMedia().stop();
						}
					}
				});
				
				loopCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
						media.setLoop(newValue);
						
					}
				});
				
			} catch (Exception e) {
				handleError("initializing", e);
			}	
			
		}
		

	}
	

	
	static String formatTime(Duration elapsed, Duration duration) {
		String elapsedTime = formatTime(elapsed);
		if (duration != null && duration.greaterThan(Duration.ZERO)) {
			return String.format("%s/%s", elapsedTime, formatTime(duration));
		} else {
			return elapsedTime;
		}
	}
	
	static String formatTime(Duration elapsed) {
		int intElapsed = (int) Math.floor(elapsed.toSeconds());
		int elapsedHours = intElapsed / (60 * 60);
		if (elapsedHours > 0) {
			intElapsed -= elapsedHours * 60 * 60;
		}
		int elapsedMinutes = intElapsed / 60;
		int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;
		
		if (elapsedHours > 0) {
			return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
		} else {
			return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
		}
		
	}
	
	static double toSliderValue(Duration currentTime, Duration duration) {
		return currentTime.divide(duration.toMillis()).toMillis() * 100.0;
	}
	

	static Duration getDurationFromSliderValue(double sliderValue, Duration duration) {
		return duration.multiply(sliderValue / 100.0);
	}	
	
	
	static class DurationCell extends TableCell<AbxMediaFile, Duration> {
		@Override
		protected void updateItem(Duration item, boolean empty) {
			super.updateItem(item, empty);
			setText(item == null ? "" : formatTime(item));
		}
	};
	
	
}
