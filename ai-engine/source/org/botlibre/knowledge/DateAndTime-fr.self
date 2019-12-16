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
		case "aujourd" goto todayState;
		case "demain" goto tomorrowState;
		case "hier" goto yesterdayState;
		case "dernière" goto lastState;
		case "dernier" goto lastState;
		case "prochaine" goto nextState;
		
		case #what goto sentenceState;
		case "quel" goto sentenceState;
		case #is goto sentenceState;
		case #the goto sentenceState;
		case "que" goto sentenceState;
		case "actuelle" goto sentenceState;
		case "actuel" goto sentenceState;
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
			case "maintenant" goto timeState;
			case "a" goto timeInState;
			case punctuation goto timeState;
			
			answer Template("Il est {Date.time()} {Date.getTimeZone()}.");
		}

		state timeInState {
			case timezone goto timeInTZState;
		}

		state timeInTZState {
			case "/" goto timeInTZSlashState;
			case "maintenant" goto timeInTZState;
			case punctuation goto timeInTZState;
			
			answer Template("L'heure à {timezone} est {Date.setTimeZone(Date.time(), timezone)} {Date.getTimeZone(timezone)}.");
		}

		state timeInTZSlashState {
			case timezone2 goto timeInTZSlashTZState;
		}

		state timeInTZSlashTZState {
			case "maintenant" goto timeInTZSlashTZState;
			case punctuation goto timeInTZSlashTZState;
			
			answer Template("L'heure à { var tz = timezone.concat("/").concat(timezone2); tz; } is {Date.setTimeZone(Date.time(), tz)} {Date.getTimeZone(tz)}.");
		}

		state hourState {
			case "a" goto hourInState;
			case #is goto hourState;
			case #it goto hourState;
			case #what goto hourState;
			case "maintenant" goto hourState;
			case punctuation goto hourState;
			
			answer Template("Il est { currentTime = Date.time(); currentTime.hour; } {currentTime.am-pm}.");
		}

		state hourInState {
			case timezone goto hourInTZState;
		}

		state hourInTZState {
			case "/" goto hourInTZSlashState;
			case "maintenant" goto hourInTZState;
			case punctuation goto hourInTZState;
			
			answer Template("L'heure à {timezone} est { var currentTime = Date.setTimeZone(Date.time(), timezone); currentTime.hour; } {currentTime.am-pm} {Date.getTimeZone(timezone)}.");
		}

		state hourInTZSlashState {
			case timezone2 goto hourInTZSlashTZState;
		}

		state hourInTZSlashTZState {
			case "maintenant" goto hourInTZSlashTZState;
			case punctuation goto hourInTZSlashTZState;
			
			answer Template("L'heure à { var tz = timezone.concat("/").concat(timezone2); tz; } est {var currentTime = Date.setTimeZone(Date.time(), tz); currentTime.hour; } {currentTime.am-pm} {Date.getTimeZone(tz)}.");
		}

		state dateState {
			case #is goto dateState;
			case #it goto dateState;
			case #what goto dateState;
			case "aujourd" goto todayState;
			case "demain" goto tomorrowState;
			case "hier" goto yesterdayState;
			case punctuation goto dateState;
			
			answer Template("La date est {Date.date()}.");
		}

		state dayState {
			case #is goto dayState;
			case #it goto dayState;
			case #what goto dayState;
			case "aujourd" goto todayState;
			case "demain" goto tomorrowState;
			case "hier" goto yesterdayState;
			case "de" goto dayState;
			case #the goto dayState;
			case "ce" goto dayState;
			case "cette" goto dayState;
			case "la" goto dayState;
			case "le" goto dayState;
			case "semaine" goto dayOfWeekState;
			case #month goto dayOfMonthState;
			case #year goto dayOfYearState;
			case punctuation goto dayState;
			
			answer Template("Le jour est {Date.date()}.");
		}

		state dayOfWeekState {
			case #is goto dayOfWeekState;
			case #it goto dayOfWeekState;
			case #what goto dayOfWeekState;
			case "aujourd" goto dayOfWeekState;
			case "'" goto dayOfWeekState;
			case "hui" goto dayOfWeekState;
			case punctuation goto dayOfWeekState;
			
			answer Template("C'est {Date.date().day-of-week}.");
		}

		state dayOfMonthState {
			case #is goto dayOfMonthState;
			case #it goto dayOfMonthState;
			case #what goto dayOfMonthState;
			case "aujourd" goto dayOfMonthState;
			case "'" goto dayOfMonthState;
			case "hui" goto dayOfMonthState;
			case punctuation goto dayOfMonthState;
			
			answer Template("C'est le {Date.date().day.ordinal} jour du mois.");
		}

		state dayOfYearState {
			case #is goto dayOfYearState;
			case #it goto dayOfYearState;
			case #what goto dayOfYearState;
			case "aujourd" goto dayOfYearState;
			case "'" goto dayOfYearState;
			case "hui" goto dayOfYearState;
			case punctuation goto dayOfYearState;
			
			answer Template("C'est le {Date.date().day-of-year.ordinal} jour de l'année.");
		}

		state todayState {
			case "'" goto todayState;
			case "s" goto todayState;
			case "date" goto todayState;
			case "hui" goto todayState;
			case #is goto todayState;
			case #it goto todayState;
			case #what goto todayState;
			case punctuation goto todayState;
			
			answer Template("Aujourd'hui, c'est {Date.date()}.");
		}

		state tomorrowState {
			case "'" goto tomorrowState;
			case "s" goto tomorrowState;
			case "date" goto tomorrowState;
			case #is goto tomorrowState;
			case #it goto tomorrowState;
			case #what goto tomorrowState;
			case punctuation goto tomorrowState;
			
			answer Template("Demain, c'est {Date.add(Date.date(), #day, 1)}.");
		}

		state yesterdayState {
			case "'" goto yesterdayState;
			case "s" goto yesterdayState;
			case "date" goto yesterdayState;
			case #is goto yesterdayState;
			case #it goto yesterdayState;
			case #what goto yesterdayState;
			case punctuation goto yesterdayState;
			
			answer Template("Hier c'était {Date.add(Date.date(), #day, -1)}.");
		}

		state lastState {
			case "jour" goto yesterdayState;
			case "mois" goto lastMonthState;
			case "semaine" goto lastWeekState;
			case "année" goto lastYearState;
		}

		state lastMonthState {
			case #is goto monthState;
			case #it goto monthState;
			case #what goto monthState;
			case punctuation goto monthState;
			
			answer Template("Le mois dernier était {(Date.add(Date.date(), #month, -1)).month}.");
		}

		state lastWeekState {
			case #is goto lastWeekState;
			case #it goto lastWeekState;
			case #what goto lastWeekState;
			case punctuation goto lastWeekState;
			
			answer Template("La semaine dernière était {Date.add(Date.date(), #week, -1)}.");
		}

		state lastYearState {
			case #is goto lastYearState;
			case #it goto lastYearState;
			case #what goto lastYearState;
			case punctuation goto lastYearState;
			
			answer Template("L'année dernière était {Date.add(Date.date(), #year, -1).year}.");
		}

		state nextState {
			case "jour" goto tomorrowState;
			case "mois" goto nextMonthState;
			case "semaine" goto nextWeekState;
			case "année" goto nextYearState;
		}

		state nextMonthState {
			case #is goto nextMonthState;
			case #it goto nextMonthState;
			case #what goto nextMonthState;
			case punctuation goto nextMonthState;
			
			answer Template("Le mois prochain {Date.add(Date.date(), #month, 1).month}.");
		}

		state nextWeekState {
			case #is goto nextWeekState;
			case #it goto nextWeekState;
			case #what goto nextWeekState;
			case punctuation goto nextWeekState;
			
			answer Template("La semaine prochaine est {Date.add(Date.date(), #week, 1)}.");
		}

		state nextYearState {
			case #is goto nextYearState;
			case #it goto nextYearState;
			case #what goto nextYearState;
			case punctuation goto nextYearState;
			
			answer Template("L'année prochaine est {Date.add(Date.date(), #year, 1).year}.");
		}

		state monthState {
			case #is goto monthState;
			case #it goto monthState;
			case #what goto monthState;
			case punctuation goto monthState;
			
			answer Template("Le mois est {Date.date().month}.");
		}

		state yearState {
			case #is goto yearState;
			case #it goto yearState;
			case #what goto yearState;
			case punctuation goto yearState;
			
			answer Template("L'année est {Date.date().year}.");
		}
	}
}

