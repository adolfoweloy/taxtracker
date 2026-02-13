# Tax scripts

I have created this repository for my own purposes. Banks usually provide reports in very different formats with the worst being PDF files.
Manually copying the data from PDFs to a spreadsheet while preparing tax report can be too laborious, so I decided to write some python scripts to automate this for me.

## Why leaving it public then

1. It can be easily shared with friends who may use same reports as mine (from Brazilian banks)
2. Friends reading this repo can suggest better ways for me to achieve the same.
3. Why not sharing?

## Current available script(s)

1. `foreign_income_br_cdb.py`: this script reads reports provided by Bradesco informing statement monthly allowing me to find the interests earned for a given financial year.

## Running

### Requirements

- Python 3 installed on your machine.

### Setup

```bash
cd tax_scripts
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

### IntelliJ IDEA Setup

1. Go to **File → Project Structure → Modules**
2. Click **+** → **Import Module**
3. Select the `tax_scripts` directory
4. Choose **Create module from existing sources** and complete the wizard
5. Set the module SDK to `tax_scripts/.venv/bin/python` (requires the **Python Community Edition** plugin)

### Usage

For more information on how to use the script, you can run:

```bash
$ python ./foreign_income_br_cdb.py --help
```

Output:
```
usage: foreign_income_br_cdb.py [-h] [--dry-run] [directory]

Process financial data from PDFs

positional arguments:
  directory   Directory containing PDF files

options:
  -h, --help  show this help message and exit
  --dry-run   Run without making external API calls
```
