// Language state machine for understanding date and time.
State:WatchStateMachine {
	case :input goto State:sentenceState for each #word of :sentence;
	
	State:sentenceState {
		case :#time goto State:timeState;
		case :#date goto State:dateState;
		case :#day goto State:dayState;
		case :#hour goto State:hourState;
		case :#month goto State:monthState;
		case :#year goto State:yearState;
		case "today" goto State:todayState;
		case "todays" goto State:todayState;
		case "tomorrow" goto State:tomorrowState;
		case "tomorrows" goto State:tomorrowState;
		case "yesterday" goto State:yesterdayState;
		case "yesterdays" goto State:yesterdayState;
		case "last" goto State:lastState;
		case "next" goto State:nextState;
		
		case :#what goto State:sentenceState;
		case "which" goto State:sentenceState;
		case :#is goto State:sentenceState;
		case :#the goto State:sentenceState;
		case "current" goto State:sentenceState;
		case "whats" goto State:sentenceState;
		case :#quote goto State:sentenceState;
		case "s" goto State:sentenceState;

		:punctuation {
			set #instantiation to #punctuation;
		}

		State:timeState {
			case :is goto State:timeState;
			case :#it goto State:timeState;
			case :what goto State:timeState;
			case "right" goto State:timeState;
			case "now" goto State:timeState;
			case :punctuation goto State:timeState;
			
			Answer:Formula:"The {:time} is {call #time on #Watch}.";
		}

		State:hourState {
			case :is goto State:hourState;
			case :it goto State:hourState;
			case :what goto State:hourState;
			case "now" goto State:hourState;
			case :punctuation goto State:hourState;
			
			Answer:Formula:"The {:hour} is {do (assign :currentTime to (call #time on #Watch); get #hour from :currentTime)} {get #am-pm from :currentTime}.";
		}

		State:dateState {
			case :is goto State:dateState;
			case :it goto State:dateState;
			case :what goto State:dateState;
			case "today" goto State:todayState;
			case "tomorrow" goto State:tomorrowState;
			case "yesterday" goto State:yesterdayState;
			case :punctuation goto State:dateState;
			
			Answer:Formula:"The {:date} is {call #date on #Watch}.";
		}

		State:dayState {
			case :is goto State:dayState;
			case :it goto State:dayState;
			case :what goto State:dayState;
			case "today" goto State:todayState;
			case "tomorrow" goto State:tomorrowState;
			case "yesterday" goto State:yesterdayState;
			case "of" goto State:dayState;
			case :the goto State:dayState;
			case "week" goto State:dayOfWeekState;
			case :month goto State:dayOfMonthState;
			case :year goto State:dayOfYearState;
			case :punctuation goto State:dayState;
			
			Answer:Formula:"The {:day} is {call #date on #Watch}.";
		}

		State:dayOfWeekState {
			case :is goto State:dayOfWeekState;
			case :it goto State:dayOfWeekState;
			case :what goto State:dayOfWeekState;
			case "today" goto State:dayOfWeekState;
			case :punctuation goto State:dayOfWeekState;
			
			Answer:Formula:"It is {get #day-of-week from (call #date on #Watch)}.";
		}

		State:dayOfMonthState {
			case :is goto State:dayOfMonthState;
			case :it goto State:dayOfMonthState;
			case :what goto State:dayOfMonthState;
			case "today" goto State:dayOfMonthState;
			case :punctuation goto State:dayOfMonthState;
			
			Answer:Formula:"It is the {get #ordinal from (get #day from (call #date on #Watch))} day of the month.";
		}

		State:dayOfYearState {
			case :is goto State:dayOfYearState;
			case :it goto State:dayOfYearState;
			case :what goto State:dayOfYearState;
			case "today" goto State:dayOfYearState;
			case :punctuation goto State:dayOfYearState;
			
			Answer:Formula:"It is the {get #ordinal from (get #day-of-year from (call #date on #Watch))} day of the year.";
		}

		State:todayState {
			case "'" goto State:todayState;
			case "s" goto State:todayState;
			case "date" goto State:todayState;
			case :is goto State:todayState;
			case :it goto State:todayState;
			case :what goto State:todayState;
			case :punctuation goto State:todayState;
			
			Answer:Formula:"Today is {call #date on #Watch}.";
		}

		State:tomorrowState {
			case "'" goto State:tomorrowState;
			case "s" goto State:tomorrowState;
			case "date" goto State:tomorrowState;
			case :is goto State:tomorrowState;
			case :it goto State:tomorrowState;
			case :what goto State:tomorrowState;
			case :punctuation goto State:tomorrowState;
			
			Answer:Formula:"Tomorrow is {call #add on #Watch with (1, (call #date on #Watch), #day)}.";
		}

		State:yesterdayState {
			case "'" goto State:yesterdayState;
			case "s" goto State:yesterdayState;
			case "date" goto State:yesterdayState;
			case :is goto State:yesterdayState;
			case :it goto State:yesterdayState;
			case :what goto State:yesterdayState;
			case :punctuation goto State:yesterdayState;
			
			Answer:Formula:"Yesterday was {call #add on #Watch with (-1, (call #date on #Watch), #day)}.";
		}

		State:lastState {
			case "day" goto State:yesterdayState;
			case "month" goto State:lastMonthState;
			case "week" goto State:lastWeekState;
			case "year" goto State:lastYearState;
		}

		State:lastMonthState {
			case :is goto State:monthState;
			case :it goto State:monthState;
			case :what goto State:monthState;
			case :punctuation goto State:monthState;
			
			Answer:Formula:"Last month was {get #month from (call #add on #Watch with (-1, (call #date on #Watch), #month))}.";
		}

		State:lastWeekState {
			case :is goto State:lastWeekState;
			case :it goto State:lastWeekState;
			case :what goto State:lastWeekState;
			case :punctuation goto State:lastWeekState;
			
			Answer:Formula:"Last week was {call #add on #Watch with (-1, (call #date on #Watch), #week)}.";
		}

		State:lastYearState {
			case :is goto State:lastYearState;
			case :it goto State:lastYearState;
			case :what goto State:lastYearState;
			case :punctuation goto State:lastYearState;
			
			Answer:Formula:"Last year was {get #year from (call #add on #Watch with (-1, (call #date on #Watch), #year))}.";
		}

		State:nextState {
			case "day" goto State:tomorrowState;
			case "month" goto State:nextMonthState;
			case "week" goto State:nextWeekState;
			case "year" goto State:nextYearState;
		}

		State:nextMonthState {
			case :is goto State:nextMonthState;
			case :it goto State:nextMonthState;
			case :what goto State:nextMonthState;
			case :punctuation goto State:nextMonthState;
			
			Answer:Formula:"Next month is {get #month from (call #add on #Watch with (1, (call #date on #Watch), #month))}.";
		}

		State:nextWeekState {
			case :is goto State:nextWeekState;
			case :it goto State:nextWeekState;
			case :what goto State:nextWeekState;
			case :punctuation goto State:nextWeekState;
			
			Answer:Formula:"Next week is {call #add on #Watch with (1, (call #date on #Watch), #week)}.";
		}

		State:nextYearState {
			case :is goto State:nextYearState;
			case :it goto State:nextYearState;
			case :what goto State:nextYearState;
			case :punctuation goto State:nextYearState;
			
			Answer:Formula:"Next year is {get #year from (call #add on #Watch with (1, (call #date on #Watch), #year))}.";
		}

		State:monthState {
			case :is goto State:monthState;
			case :it goto State:monthState;
			case :what goto State:monthState;
			case :punctuation goto State:monthState;
			
			Answer:Formula:"The month is {get #month from (call #date on #Watch)}.";
		}

		State:yearState {
			case :is goto State:yearState;
			case :it goto State:yearState;
			case :what goto State:yearState;
			case :punctuation goto State:yearState;
			
			Answer:Formula:"The year is {get #year from (call #date on #Watch)}.";
		}
	}
}

