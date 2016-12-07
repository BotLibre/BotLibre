// Language state machine for understanding date and time.
state DateAndTime {
	case input goto sentenceState for each #word of sentence;
	
	state sentenceState {
		case #time goto timeState;
		case #date goto dateState;
		case #day goto dayState;
		case #hour goto hourState;
		case #month goto monthState;
		case #year goto yearState;
		case "today" goto todayState;
		case "todays" goto todayState;
		case "tomorrow" goto tomorrowState;
		case "tomorrows" goto tomorrowState;
		case "yesterday" goto yesterdayState;
		case "yesterdays" goto yesterdayState;
		case "last" goto lastState;
		case "next" goto nextState;
		
		case #what goto sentenceState;
		case "which" goto sentenceState;
		case #is goto sentenceState;
		case #the goto sentenceState;
		case "this" goto sentenceState;
		case "current" goto sentenceState;
		case "whats" goto sentenceState;
		case #quote goto sentenceState;
		case "s" goto sentenceState;

		var punctuation {
			instantiation : #punctuation;
		}

		state timeState {
			case #is goto timeState;
			case #it goto timeState;
			case #what goto timeState;
			case "right" goto timeState;
			case "now" goto timeState;
			case "in" goto timeInState;
			case punctuation goto timeState;
			
			answer Template("The time is {Date.time()} {Date.getTimeZone()}.");
		}

		state timeInState {
			case timezone goto timeInTZState;
		}

		state timeInTZState {
			case "/" goto timeInTZSlashState;
			case "right" goto timeInTZState;
			case "now" goto timeInTZState;
			case punctuation goto timeInTZState;
			
			answer Template("The time in {timezone} is {Date.setTimeZone(Date.time(), timezone)} {Date.getTimeZone(timezone)}.");
		}

		state timeInTZSlashState {
			case timezone2 goto timeInTZSlashTZState;
		}

		state timeInTZSlashTZState {
			case "right" goto timeInTZSlashTZState;
			case "now" goto timeInTZSlashTZState;
			case punctuation goto timeInTZSlashTZState;
			
			answer Template("The time in { var tz = timezone.concat("/").concat(timezone2); tz; } is {Date.setTimeZone(Date.time(), tz)} {Date.getTimeZone(tz)}.");
		}

		state hourState {
			case "in" goto hourInState;
			case #is goto hourState;
			case #it goto hourState;
			case #what goto hourState;
			case "now" goto hourState;
			case punctuation goto hourState;
			
			answer Template("The hour is { currentTime = Date.time(); currentTime.hour; } {currentTime.am-pm}.");
		}

		state hourInState {
			case timezone goto hourInTZState;
		}

		state hourInTZState {
			case "/" goto hourInTZSlashState;
			case "right" goto hourInTZState;
			case "now" goto hourInTZState;
			case punctuation goto hourInTZState;
			
			answer Template("The hour in {timezone} is { var currentTime = Date.setTimeZone(Date.time(), timezone); currentTime.hour; } {currentTime.am-pm} {Date.getTimeZone(timezone)}.");
		}

		state hourInTZSlashState {
			case timezone2 goto hourInTZSlashTZState;
		}

		state hourInTZSlashTZState {
			case "right" goto hourInTZSlashTZState;
			case "now" goto hourInTZSlashTZState;
			case punctuation goto hourInTZSlashTZState;
			
			answer Template("The hour in { var tz = timezone.concat("/").concat(timezone2); tz; } is {var currentTime = Date.setTimeZone(Date.time(), tz); currentTime.hour; } {currentTime.am-pm} {Date.getTimeZone(tz)}.");
		}

		state dateState {
			case #is goto dateState;
			case #it goto dateState;
			case #what goto dateState;
			case "today" goto todayState;
			case "tomorrow" goto tomorrowState;
			case "yesterday" goto yesterdayState;
			case punctuation goto dateState;
			
			answer Template("The date is {Date.date()}.");
		}

		state dayState {
			case #is goto dayState;
			case #it goto dayState;
			case #what goto dayState;
			case "today" goto todayState;
			case "tomorrow" goto tomorrowState;
			case "yesterday" goto yesterdayState;
			case "of" goto dayState;
			case #the goto dayState;
			case "this" goto dayState;
			case "week" goto dayOfWeekState;
			case #month goto dayOfMonthState;
			case #year goto dayOfYearState;
			case punctuation goto dayState;
			
			answer Template("The day is {Date.date()}.");
		}

		state dayOfWeekState {
			case #is goto dayOfWeekState;
			case #it goto dayOfWeekState;
			case #what goto dayOfWeekState;
			case "today" goto dayOfWeekState;
			case punctuation goto dayOfWeekState;
			
			answer Template("It is {Date.date().day-of-week}.");
		}

		state dayOfMonthState {
			case #is goto dayOfMonthState;
			case #it goto dayOfMonthState;
			case #what goto dayOfMonthState;
			case "today" goto dayOfMonthState;
			case punctuation goto dayOfMonthState;
			
			answer Template("It is the {Date.date().day.ordinal} day of the month.");
		}

		state dayOfYearState {
			case #is goto dayOfYearState;
			case #it goto dayOfYearState;
			case #what goto dayOfYearState;
			case "today" goto dayOfYearState;
			case punctuation goto dayOfYearState;
			
			answer Template("It is the {Date.date().day-of-year.ordinal} day of the year.");
		}

		state todayState {
			case "'" goto todayState;
			case "s" goto todayState;
			case "date" goto todayState;
			case #is goto todayState;
			case #it goto todayState;
			case #what goto todayState;
			case punctuation goto todayState;
			
			answer Template("Today is {Date.date()}.");
		}

		state tomorrowState {
			case "'" goto tomorrowState;
			case "s" goto tomorrowState;
			case "date" goto tomorrowState;
			case #is goto tomorrowState;
			case #it goto tomorrowState;
			case #what goto tomorrowState;
			case punctuation goto tomorrowState;
			
			answer Template("Tomorrow is {Date.add(Date.date(), #day, 1)}.");
		}

		state yesterdayState {
			case "'" goto yesterdayState;
			case "s" goto yesterdayState;
			case "date" goto yesterdayState;
			case #is goto yesterdayState;
			case #it goto yesterdayState;
			case #what goto yesterdayState;
			case punctuation goto yesterdayState;
			
			answer Template("Yesterday was {Date.add(Date.date(), #day, -1)}.");
		}

		state lastState {
			case "day" goto yesterdayState;
			case "month" goto lastMonthState;
			case "week" goto lastWeekState;
			case "year" goto lastYearState;
		}

		state lastMonthState {
			case #is goto monthState;
			case #it goto monthState;
			case #what goto monthState;
			case punctuation goto monthState;
			
			answer Template("Last month was {(Date.add(Date.date(), #month, -1)).month}.");
		}

		state lastWeekState {
			case #is goto lastWeekState;
			case #it goto lastWeekState;
			case #what goto lastWeekState;
			case punctuation goto lastWeekState;
			
			answer Template("Last week was {Date.add(Date.date(), #week, -1)}.");
		}

		state lastYearState {
			case #is goto lastYearState;
			case #it goto lastYearState;
			case #what goto lastYearState;
			case punctuation goto lastYearState;
			
			answer Template("Last year was {Date.add(Date.date(), #year, -1).year}.");
		}

		state nextState {
			case "day" goto tomorrowState;
			case "month" goto nextMonthState;
			case "week" goto nextWeekState;
			case "year" goto nextYearState;
		}

		state nextMonthState {
			case #is goto nextMonthState;
			case #it goto nextMonthState;
			case #what goto nextMonthState;
			case punctuation goto nextMonthState;
			
			answer Template("Next month is {Date.add(Date.date(), #month, 1).month}.");
		}

		state nextWeekState {
			case #is goto nextWeekState;
			case #it goto nextWeekState;
			case #what goto nextWeekState;
			case punctuation goto nextWeekState;
			
			answer Template("Next week is {Date.add(Date.date(), #week, 1)}.");
		}

		state nextYearState {
			case #is goto nextYearState;
			case #it goto nextYearState;
			case #what goto nextYearState;
			case punctuation goto nextYearState;
			
			answer Template("Next year is {Date.add(Date.date(), #year, 1).year}.");
		}

		state monthState {
			case #is goto monthState;
			case #it goto monthState;
			case #what goto monthState;
			case punctuation goto monthState;
			
			answer Template("The month is {Date.date().month}.");
		}

		state yearState {
			case #is goto yearState;
			case #it goto yearState;
			case #what goto yearState;
			case punctuation goto yearState;
			
			answer Template("The year is {Date.date().year}.");
		}
	}
}
