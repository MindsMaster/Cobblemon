import os
import time
import requests
import pandas as pd
import json
from io import StringIO
from sqlalchemy import create_engine
from tqdm import tqdm
from scriptutils import printCobblemonHeader, print_cobblemon_script_description, print_cobblemon_script_footer, \
    print_list_filtered, print_warning, sanitize_pokemon

# Initialize lists for report
no_behaviour_base_forms = []
removed_behaviour_pokemon = []
no_form_entry_files = []
no_behaviour_forms = []

form_entry_mapping = {
    "Alolan": "Alola",
    "Galarian": "Galar",
    "Hisuian": "Hisui",
    "Paldean": "Paldea",
}
#
# controller_keys = {
#     'Standard': 'cobblemon:land/generic',
#     'Vehicle': 'cobblemon:land/vehicle',
#     #'Submarine': 'cobblemon:water/submarine',
#     #'Dolphin': 'cobblemon:water/dolphin',
#     #'Burst': 'cobblemon:water/burst',
#     'Boat': 'cobblemon:water/boat',
#     #'Roll': 'cobblemon:land/roll',
#     #'Jet': 'cobblemon:air/jet',
#     'Bird': 'cobblemon:air/bird',
#     #'UFO': 'cobblemon:air/ufo',
#     'Glider': 'cobblemon:air/glider',
#     #'Airship': 'cobblemon:air/airship',
#     'Fall To Flight': 'cobblemon:composite/fall_to_flight',
#     'Run Up To Flight': 'cobblemon:composite/run_up_to_flight'
# }

def main(dex_range=None):
    """
    The behaviour_csv to json script does the following:
    1. Retrieves the DataFrame containing the riding data.
    2. Filters the filenames by Pokémon names contained in riding df.
    3. Modifies the files based on the riding data.
    4. Writes the modified data back to the files.
    """
    # Print header
    printCobblemonHeader()
    script_name = "Cobblemon Pokémon Behaviour Generator"
    script_description = "This script generates the behaviour data for each Pokémon based on the data in a Google Spreadsheet. It also generates the behaviour properties for each form of a Pokémon, if the Pokémon has any forms and if the forms behaviour settings are different from the base form. It also generates a report, hinting at potential problems with the data in the Google Spreadsheet or in the JSON files."
    print_cobblemon_script_description(script_name, script_description)

    # Retrieve the DataFrame containing the data
    behaviour_df, pokemon_data_dir = get_behaviour_df(dex_range)

    # Add a forms column to the DataFrame, and sanitize the Pokémon names
    print(behaviour_df)
    behaviour_df['forms'] = behaviour_df['Pokémon'].apply(lambda x: x.split("[")[1].split("]")[0] if "[" in x else None)
    behaviour_df['Species'] = behaviour_df['Pokémon'].apply(lambda san: sanitize_pokemon(san.split("[")[0].strip()))

    # Group the behaviour data by Pokémon names, then convert it to a dictionary
    behaviour_dict = behaviour_df.groupby('Species').apply(lambda x: x.to_dict(orient='records'),
                                                   include_groups=False).to_dict()
    # Filter the filenames by Pokémon names
    files_to_change = filter_filenames_by_pokemon_names(pokemon_data_dir, behaviour_df['Species'])

    # Modify the files based on the data
    print_warning("Modifying files...")
    for file in tqdm(files_to_change, bar_format='\033[92m' + '{l_bar}\033[0m{bar:58}\033[92m{r_bar}\033[0m',colour='blue'):
        try:
            modify_files(file, pokemon_data_dir, behaviour_dict)
        except Exception as e:
            print_warning(f"Error modifying {file}: {e}")
            raise e

    # Print report
    if no_behaviour_base_forms:
        print("\nNo behaviour specified for base forms in the sheet, but species files exists:")
        print_list_filtered(no_behaviour_base_forms)
    if no_behaviour_forms:
        print("\nNo behaviour specified for forms in the sheet, even though form entries exist in .json files:")
        print_list_filtered(no_behaviour_forms)
    if removed_behaviour_pokemon:
        print("\nRemoved behaviour specified for Pokémon in the sheet:")
        print_list_filtered(removed_behaviour_pokemon)
    if no_form_entry_files:
        print("\nNo form entry found in the respective .json file for the following pokemon, but form behaviour properties were specified in the sheet:")
        print_list_filtered(no_form_entry_files)
    print_cobblemon_script_footer("Thanks for using the Cobblemon Pokémon Behaviour Generator, provided to you by Hiroku and Waldleufer!")



def get_behaviour_df(dex_range=range(0, 1111)):
    behaviour_spreadsheet_csv_url = 'https://docs.google.com/spreadsheets/d/e/2PACX-1vQ97ey7CNpCkFJDPceEKGTT2g0XeIpvWRIGy8wnc2d95cOZSW9XEXfU94_VkR-TQCfgFNEsjHGxp6a6/pub?gid=0&single=true&output=csv'
    pokemon_data_dir = '../common/src/main/resources/data/cobblemon/species'
    # Download the CSV from the Google Spreadsheet
    csv_data = download_spreadsheet_data(behaviour_spreadsheet_csv_url)
    # Load the data into a DataFrame
    behaviour_df = load_data_from_csv(csv_data)

    # Only include the specified range of dex numbers
    if dex_range is not None:
        behaviour_df = behaviour_df[behaviour_df['No.'].isin(dex_range)]

    return behaviour_df, pokemon_data_dir


def modify_files(file, pokemon_data_dir, behaviour_dict):
    # Open the file with 'utf-8-sig' encoding and read its contents
    with open(pokemon_data_dir + "/" + file, 'r', encoding="utf-8-sig") as f:
        data = json.load(f)

    # make sure to write the file with utf-8 encoding
    with open(pokemon_data_dir + "/" + file, 'r+', encoding="utf-8") as f:
        pokemon = sanitize_pokemon(data['name'])

        # compare all forms of the file with all forms of the behaviour_dict pokemon
        file_forms = set(form['name'] for form in data['forms']) if 'forms' in data else set()
        # always add None to the file_forms set, as the base form is always present in the files
        file_forms.add(None)
        behaviour_dict_forms = set(row['forms'] for row in behaviour_dict[pokemon] if 'forms' in row)
        # apply the mapping to al the behaviour_forms
        behaviour_dict_forms = set(form_entry_mapping.get(form, form) for form in behaviour_dict_forms)

        # Add forms that are in the file but not in behaviour_dict to no_behaviour_forms
        difference = file_forms - behaviour_dict_forms
        no_behaviour_forms.extend(f'{pokemon} [{form}]' for form in difference)

        # Add forms that are in behaviour_dict but not in the file to no_form_entry_files
        no_form_entry_files.extend(f'{pokemon} [{form}]' for form in behaviour_dict_forms - file_forms)

        for row in behaviour_dict[pokemon]:
            pokemon_form = row['forms'] if 'forms' in row else None
            blank_behaviour = False #has_behaviour(row) == False

            if pokemon in behaviour_dict:
                none_existed = False
                if 'forms' in data:
                    for form in data['forms']:
                        # There was an explicit 'None' form? Bet, won't need to do a later process
                        if form['name'] == 'None':
                            none_existed = True
                        # Apply the form entry mapping to the form name if applicable
                        if pokemon_form in form_entry_mapping:
                            pokemon_form = form_entry_mapping[pokemon_form]
                        if form['name'] == pokemon_form:
                            if blank_behaviour == True:
                                no_behaviour_forms.append(f'{pokemon} [{pokemon_form}]')
                                form.pop('behaviour', None)
                            else:
                                apply_behaviour(form, row)
                if none_existed == False and pokemon_form == None:
                    if blank_behaviour:
                        no_behaviour_base_forms.append(file)
                        data.pop('behaviour', None)
                    else:
                        apply_behaviour(data, row)
                # Clean duplicates from forms
                if 'forms' in data:
                    for form in data['forms']:
                        if 'behaviour' in form and 'behaviour' in data:
                            if form['behaviour'] == data['behaviour']:
                                form.pop('behaviour')
        # Write the modified data back to the file
        f.seek(0)
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.truncate()

################################################################################
# All of the functions below for building out parts of the behaviour data are
# designed against the defaults for all of the fields, hoping to minimise how
# much actually gets written into the JSON. Naturally, if these defaults get
# changed in Cobblemon we'll probably want to update the script to respect that.
################################################################################
def apply_behaviour(pokemon_form, behaviour_row):
    if 'behaviour' not in pokemon_form:
        pokemon_form['behaviour'] = {}

    behaviour = pokemon_form['behaviour']

    resting = build_resting(behaviour_row)
    if resting:
        behaviour['resting'] = resting
    moving = build_moving(behaviour_row)
    if moving:
        behaviour['moving'] = moving
    idle = build_idle(behaviour_row)
    if idle:
        behaviour['idle'] = idle
    entity_interact = build_entity_interact(behaviour_row)
    if entity_interact:
        behaviour['entityInteract'] = entity_interact
    combat = build_combat(behaviour_row)
    if combat:
        behaviour['combat'] = combat
    herd = build_herd(behaviour_row)
    if herd:
        behaviour['herd'] = herd
    if behaviour_row['Hurt by Lava'] == False:
        behaviour['fireImmune'] = True

def build_moving(behaviour_row):
    moving = {}
    walk = build_walk(behaviour_row)
    if walk:
        moving['walk'] = walk
    swim = build_swim(behaviour_row)
    if swim:
        moving['swim'] = swim
    fly = build_fly(behaviour_row)
    if fly:
        moving['fly'] = fly
    # moving['stepHeight'] = behaviour_row['Step Height']
    # moving['wanderChance'] = behaviour_row['Wander Chance']
    # moving['wanderSpeed'] = behaviour_row['Wander Speed']
    if behaviour_row['Look'] == False:
        moving['canLook'] = False
    return moving

def build_walk(behaviour_row):
    walk = {}
    if behaviour_row['Walk'] == False:
        walk['canWalk'] = False
    if behaviour_row['Walk Speed'] != '':
        walk['walkSpeed'] = '"' + behaviour_row['Walk Speed'] + '"'
    if behaviour_row['Avoids Land'] != False:
        walk['avoidsLand'] = True
    return walk

def build_swim(behaviour_row):
    swim = {}
    if behaviour_row['Avoids Water'] == True:
        swim['avoidsWater'] = True
    if behaviour_row['W. Swim'] == False:
        swim['canSwimInWater'] = False
    if behaviour_row['Swim Speed'] != '':
        swim['swimSpeed'] = '"' + behaviour_row['Swim Speed'] + '"'
    if behaviour_row['W. Breathing'] == True:
        swim['canBreatheUnderwater'] = True
    if behaviour_row['W. Walk'] == True:
        swim['canWalkOnWater'] = True

    if behaviour_row['L. Swim'] == True:
        swim['canSwimInLava'] = True
    if behaviour_row['L. Walk'] == True:
        swim['canWalkOnLava'] = True
    if behaviour_row['L. Breathing'] == True:
        swim['canBreatheInLava'] = True

    return swim

def build_fly(behaviour_row):
    fly = {}
    if behaviour_row['Fly'] == True:
        fly['canFly'] = True
    if behaviour_row['Fly Speed'] != '':
        fly['flySpeedHorizontal'] = '"' + behaviour_row['Fly Speed'] + '"'
    return fly

def build_resting(behaviour_row):
    resting = {}
    if behaviour_row['Sleep'] == True:
        resting['canSleep'] = True
    if behaviour_row['S. Depth'] != '':
        resting['depth'] = behaviour_row['S. Depth']
    if behaviour_row['S. Times'] != '' and behaviour_row['S. Times'] != 'Any':
        resting['times'] = behaviour_row['S. Times'].lower().split('/')
    if behaviour_row['S. Light'] != '':
        resting['light'] = behaviour_row['S. Light']
    if behaviour_row['S. Blocks'] != '':
        resting['blocks'] = behaviour_row['S. Blocks'].lower().split(',')
    if behaviour_row['S. Biomes'] != '':
        resting['biomes'] = behaviour_row['S. Biomes'].lower().split(',')
    if behaviour_row['Bed S.'] == True:
        resting['willSleepOnBed'] = True
    if behaviour_row['S. Sees Sky'] == True:
        resting['canSeeSky'] = True
    if behaviour_row['S. Sees Sky'] == False:
        resting['canSeeSky'] = False
    if behaviour_row['S. Skylight'] != '':
        resting['skyLight'] = behaviour_row['S. Skylight']
    return resting

def build_idle(behaviour_row):
    return {}

def build_entity_interact(behaviour_row):
    entity_interact = {}
    if 'avoidedBySkeleton' in behaviour_row['Species Specific']:
        entity_interact['avoidedBySkeleton'] = True
    if 'avoidedByCreeper' in behaviour_row['Species Specific']:
        entity_interact['avoidedByCreeper'] = True
    # TODO avoidedByPhantom
    return entity_interact

def build_combat(behaviour_row):
    combat = {}
    if behaviour_row['Defends Self'] == True:
        combat['willDefendSelf'] = True
    if behaviour_row['Defends Owner'] == True:
        combat['willDefendOwner'] = True
    if behaviour_row['Will Flee'] == True:
        combat['willFlee'] = True
    if ('willDefendSelf' in combat and combat['willDefendSelf']) or ('willDefendOwner' in combat and combat['willDefendOwner']):
        combat['fightsMelee'] = True # Until we have ranged combat
    return combat

def build_herd(behaviour_row):
    herd = {}
    if behaviour_row['maxSize'] != '':
        herd['maxSize'] = behaviour_row['maxSize']
    if behaviour_row['Follow Distance'] != '':
        herd['followDistance'] = behaviour_row['Follow Distance']
    if behaviour_row['Follows'] != '':
        leaders = []
        strings = behaviour_row['Follows'].split(',')
        for s in strings:
            s = s.strip()
            # format: <some_pokemon_information>-<tier>
            tier = int(s.split('-')[-1].strip() if '-' in s else '1')
            pokemon = sanitize_pokemon(s.split('-')[0].strip())
            leaders.append({'pokemon': pokemon, 'tier': tier})
        if leaders:
            herd['toleratedLeaders'] = leaders
    return herd

def download_spreadsheet_data(url, max_retries=10):
    delay = 1
    for attempt in range(max_retries):
        try:
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            return response.content.decode('utf-8')
        except requests.RequestException as e:
            if attempt < max_retries - 1:
                time.sleep(delay)
                delay *= 2
            else:
                raise e


def load_data_from_csv(csv_data):
    return pd.read_csv(StringIO(csv_data), encoding='utf8', engine='python', skiprows=1, dtype={'Species': str, 'Composite': str, 'Water': str, 'Flying': str, 'Special': str }, na_filter=False)

def filter_filenames_by_pokemon_names(directory, pokemon_names):
    # Apply the sanitize_pokemon function to the pokemon_names
    pokemon_names = pokemon_names.apply(sanitize_pokemon)

    # Get list of subdirectories in the provided directory
    subdirectories = [d for d in os.listdir(directory) if os.path.isdir(os.path.join(directory, d))]

    all_files = []
    for subdir in subdirectories:
        # List all files from the subdirectory
        files_in_subdir = os.listdir(os.path.join(directory, subdir))
        # Extend the all_files list with these files
        # While adding, prepend the subdirectory name
        all_files.extend([f"{subdir}/{file}" for file in files_in_subdir])

    filtered_files = [file for file in all_files if
                      file.split('/')[-1][:-5].lower() in pokemon_names.str.lower().tolist()]

    # Get the files that did not pass the filter
    not_filtered_files = [file for file in all_files if file not in filtered_files]

    if not_filtered_files:
        print("\nSpecies file found, but ignored:  [[located at resources/data/cobblemon/species/]]")
        print_list_filtered(not_filtered_files)
    return filtered_files


if __name__ == "__main__":
    dex_range = range(0, 1111)
    main(dex_range)
