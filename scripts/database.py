'''
database connection and utils
'''
import pymysql, os, re

def connect(dbName):
    conn = pymysql.connect(host='localhost', port=3306, user='root', passwd='root', db=dbName,
                           use_unicode='true', charset='utf8')
    return conn


'''
inserts the 25 pois from DBpedia dataset
'''
def insertNewPois(inFile):
    dataFile = open(inFile,"r")
    conn = connect('eval')
    cursor = conn.cursor()
    
    for line in dataFile:
        if 'res' in line:
            poi = line[line.find('res:')+4:line.find('" city')].encode('utf-8')
            cursor.execute("INSERT INTO dbpedia_pois (name) VALUES (\""+poi.replace('_',' ')+"\")".encode('utf-8'))
            conn.commit()
    
    cursor.close()
    conn.close()
    dataFile.close()

'''
inserts the ~420 tracks by the ~140 musicians from DBpedia dataset
'''
def insertNewTracks(dataDir):
    conn = connect('eval')
    cursor = conn.cursor()
    
    # this replaces all non-letter characters with % for DB matching of musicians
    tr_table=''.join(chr(c) if chr(c).isupper() or chr(c).islower() else '%' for c in range(256))
    
    for filename in os.listdir(dataDir):
        trackName = filename[filename.find('-')+1:filename.find('.mp3')].strip()
        musicianName = filename[:filename.find('-')].strip()
        musicianQuery = re.sub(r'','',musicianName.translate(tr_table))
        
        cursor.execute("SELECT musician FROM dbpedia_musicians \
                        WHERE musician LIKE '%"+musicianQuery+"%'")
        for row in cursor:
            musician = row[0]
        
        cursor2 = conn.cursor()
        cursor2.execute("INSERT INTO dbpedia_tracks (name,musician,filename) \
                        VALUES (\""+trackName+"\",\""+musician+"\",\""+filename+"\")".encode('utf-8'))
        conn.commit()
    
    cursor.close()
    cursor2.close()
    conn.close()

'''
returns the list of vocabulary tags that were found in the item's profile
'''
def checkForTags(itemName):
    foundTags = []
    conn = connect('eval')
    cursor = conn.cursor()
    cursor2 = conn.cursor()
    
    cursor.execute("SELECT tags FROM dbpedia_pois WHERE name = '"+itemName+"'")
    itemTags = str(cursor.fetchone())
    
    cursor2.execute("SELECT DISTINCT tag FROM tag \
                    UNION \
                    SELECT DISTINCT type FROM tag")
    for row in cursor2:
        if row[0] in itemTags:
            foundTags.append(row[0])
    
    cursor.close()
    cursor2.close()
    conn.close()
    return foundTags

'''
check precision of DBpedia algorithms
'''
def checkDBPediaAlgorithm(algorithm):
    hits = 0
    total = 0
    conn = connect('dbpedia-recommender')
    cursor = conn.cursor()
    cursor.execute("SELECT relatedness FROM `hypertext12_experiment_cases` AS a \
                    WHERE relatedness <> '' AND ranking_"+algorithm+" <> 0 AND ranking_"+algorithm+" = \
                        (SELECT LEAST( \
                                IF(ranking_random=0,10000,ranking_random), \
                                IF(ranking_spreading=0,10000,ranking_spreading), \
                                IF(ranking_hits=0,10000,ranking_hits), \
                                IF(ranking_pagerank=0,10000,ranking_pagerank), \
                                IF(ranking_weightedpagerank=0,10000,ranking_weightedpagerank) \
                                ) \
                        FROM `hypertext12_experiment_cases` AS b \
                        WHERE a.id = b.id)")
    for row in cursor:
        total += 1
        if row[0]=='very_related' or row[0]=='related' or row[0]=='poor_related':
            hits += 1
    
    return float(hits)/float(total)


'''
prints the tag profiles of POIs/music for Weka analysis
'''
def printTagProfiles(objectType, outFile):
    outputFile = open(outFile,"w")
    conn = connect('eval-lamj')
    cursor = conn.cursor()
    cursor2 = conn.cursor()
    
    if (objectType=='tracks'):
        query = 'SELECT id FROM one_per_musician'
    else:
        query = 'SELECT id FROM dbpedia_pois'
    
    cursor.execute(query)
    for row in cursor:
        itemId = row[0]
        cursor2.execute('SELECT tag_cleaned.tag, cunt FROM tag_cleaned \
                    LEFT JOIN \
                    (SELECT tag, COUNT(*) AS cunt \
                     FROM dbpedia_'+objectType+'_tag_cleaned tags \
                     WHERE tags.dbpedia_'+objectType+' = '+str(itemId)+' \
                     GROUP BY tag) AS '+objectType+'_tags \
                    ON tag_cleaned.tag='+objectType+'_tags.tag')
        
        tagProfileString = ''
        for row in cursor2:
            if (row[1] != None):
                tagProfileString += str(row[1])+'.0,'
            else:
                tagProfileString += '0.0,'
        
        outputFile.write(tagProfileString.rstrip(',')+"\n")
    
    cursor2.close()
    cursor.close()
    conn.close()
    outputFile.close()


if __name__ == "__main__":
    
    printTagProfiles("pois","../../data/pois2.arff")
    printTagProfiles("tracks","../../data/tracks2.arff")
    
    insertNewPois("../../data/newPois.dat")
    insertNewTracks("../../data/tracks")

    print 'Random: ',checkDBPediaAlgorithm('random')
    print 'Spreading: ',checkDBPediaAlgorithm('spreading')
    print 'Hits: ',checkDBPediaAlgorithm('hits')
    print 'PageRank: ',checkDBPediaAlgorithm('pagerank')
    print 'WeightedPageRank: ',checkDBPediaAlgorithm('weightedpagerank')
    