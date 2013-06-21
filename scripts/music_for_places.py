'''
Created on Dec 6, 2012

Computing the ranked lists of music recommendations using the competing approaches

@author: mkaminskas
'''
import database, numpy, math, scipy.stats

def getMusicTagProfile(trackId,manualOrPredictedOrAll):
    conn = database.connect('eval-lamj')
    cursor = conn.cursor()
#    cursor.execute('SELECT '+taggerOrPredicted+'_prob FROM dbpedia_music_tags \
#                    WHERE dbpedia_track = '+str(trackId)+' \
#                    ORDER BY tag')
    cursor.execute('SELECT '+manualOrPredictedOrAll+'_tag FROM new_tracks_tags \
                    WHERE dbpedia_track = '+str(trackId)+' AND '+manualOrPredictedOrAll+'_tag IS NOT NULL')    
    tagProfile = [row[0] for row in cursor]
    return tagProfile


def getPOITagProfile(poiId):
    conn = database.connect('eval-lamj')
    cursor = conn.cursor()
#    cursor.execute('SELECT tagger_prob FROM dbpedia_place_tags \
#                    WHERE dbpedia_poi = '+str(poiId)+' \
#                    ORDER BY tag')
    cursor.execute('SELECT manual_tag FROM new_pois_tags \
                    WHERE dbpedia_poi = '+str(poiId))
    tagProfile = [row[0] for row in cursor]
    return tagProfile


def getAllTagProbs(manualOrPredictedOrAll):
    tagsAndProbs = {}
    conn = database.connect('eval-lamj')
    cursor = conn.cursor()
    cursor.execute('SELECT tag, P_'+manualOrPredictedOrAll+' FROM tag_cleaned')
    for row in cursor:
        tagsAndProbs[row[0]] = row[1]
    cursor.close()
    conn.close()
    return tagsAndProbs

def cosineSimilarity(poiId,trackId):
    similarities = {}
    similarities['manual'] = 0.0
    similarities['auto'] = 0.0
    
    manualTrackProfile = getMusicTagProfile(trackId,'tagger')
    autoTrackProfile = getMusicTagProfile(trackId,'predicted')    
    poiProfile = getPOITagProfile(poiId)
    
    # computes the cosine similarity between two vectors
    similarities['manual'] = numpy.dot(poiProfile, manualTrackProfile) / ( math.sqrt(numpy.dot(poiProfile, poiProfile)) * math.sqrt(numpy.dot(manualTrackProfile, manualTrackProfile)) )
    
    similarities['auto'] = numpy.dot(poiProfile, autoTrackProfile) / ( math.sqrt(numpy.dot(poiProfile, poiProfile)) * math.sqrt(numpy.dot(autoTrackProfile, autoTrackProfile)) )
    
    return similarities


# the weighted Jaccard similarity of manual/predicted tags
def jaccardSimilarity(poiId,trackId,manualOrPredictedOrAll,tagsAndProbs):
    
    trackProfile = getMusicTagProfile(trackId,manualOrPredictedOrAll)
    poiProfile = getPOITagProfile(poiId)
    
    intersec = set(poiProfile).intersection(trackProfile)
    uni = set(poiProfile).union(trackProfile)
    
    numerator = 0.0
    for tag in intersec:
        numerator += math.log(tagsAndProbs[tag],2)
    
    denominator = 0.0
    for tag in uni:
        denominator += math.log(tagsAndProbs[tag],2)
    
    if (denominator == 0.0):
        print 'ERROR!!!'
    
    return math.fabs(numerator / denominator)


def loadTagSimilarities():
    conn = database.connect('eval-lamj')
    cursor = conn.cursor()
    cursor2 = conn.cursor()
    
    # get the probs of tags (to be used as log(p) weights in Jaccard computation):
    tagsAndProbsManual = getAllTagProbs('manual')
    tagsAndProbsPredicted = getAllTagProbs('predicted')
    tagsAndProbsPredictedAll = getAllTagProbs('predictedAll')
    
    cursor.execute('SELECT dbpedia_pois.id, dbpedia_tracks.id, \
                        (dbpedia_tracks.id IN (SELECT id FROM one_per_musician)) AS tagged \
                    FROM dbpedia_pois JOIN dbpedia_tracks')
    for row in cursor:
        poiId = row[0]
        trackId = row[1]
        tagged = row[2]
        
        manualSimilarity = 0.0
        autoSimilarity = 0.0
        
        if (tagged == 1): # compute the manual and predicted similarities only for the 123 tracks
            manualSimilarity = jaccardSimilarity(poiId,trackId,'manual',tagsAndProbsManual)
            autoSimilarity = jaccardSimilarity(poiId,trackId,'predicted',tagsAndProbsPredicted)
        autoAllSimilarity = jaccardSimilarity(poiId,trackId,'predictedAll',tagsAndProbsPredictedAll)
        
        # inser them into dbpedia_similarity / new_similarity
        cursor2.execute("INSERT INTO new_similarity \
                        (dbpedia_poi,dbpedia_track,manual_similarity,auto_similarity,autoAll_similarity) \
                        VALUES ("+str(poiId)+","+str(trackId)+","+str(manualSimilarity)+",\
                                "+str(autoSimilarity)+","+str(autoAllSimilarity)+")")
        conn.commit()
        
    cursor.close()
    cursor2.close()
    conn.close()


def loadSemanticSimilarities():
    conn = database.connect('eval-lamj')
    cursor = conn.cursor()
    cursor2 = conn.cursor()
    
    cursor.execute('SELECT pois.id, pois.wiki_page, mus.id, mus.musician \
                    FROM dbpedia_pois pois JOIN dbpedia_tracks mus')
    for row in cursor:
        poiId = row[0]
        poi = "res:" + row[1][row[1].rfind('/')+1:]
        trackId = row[2]
        musician = row[3]
        
        score = getSemanticScore(poi,musician)
        # dbpedia_similarity / new_similarity
        cursor2.execute("UPDATE new_similarity \
                        SET semantic_similarity = "+str(score)+" \
                        WHERE dbpedia_poi="+str(poiId)+" AND dbpedia_track="+str(trackId))
        conn.commit()
        
    cursor.close()
    cursor2.close()
    conn.close()


# get the tracks' similarity to a given POI
def getTrackSimilarities(poiId, manualORauto):
    conn = database.connect('eval-lamj')
    cursor = conn.cursor()
    cursor.execute('SELECT '+manualORauto+'_similarity FROM dbpedia_similarity \
                    WHERE dbpedia_poi='+str(poiId)+' \
                    ORDER BY dbpedia_track')
    rankedTracks = [row[0] for row in cursor]
    cursor.close()
    conn.close()
    return rankedTracks



####################### combined similarity computation ############################


def loadCombinedSimilarities():
    conn = database.connect('eval-lamj')
    cursor = conn.cursor()
    cursor2 = conn.cursor()
    
    cursor.execute('SELECT pois.id, mus.id FROM dbpedia_pois pois JOIN dbpedia_tracks mus')
    for row in cursor:
        poiId = row[0]
        trackId = row[1]
        
        # get the tag-based ranking score
        tagScore = getTagScore(poiId,trackId)
        
        # dbpedia_similarity / new_similarity
        cursor2.execute("UPDATE new_similarity \
                        SET combined_similarity = semantic_similarity + "+str(tagScore)+" \
                        WHERE dbpedia_poi="+str(poiId)+" AND dbpedia_track="+str(trackId))
        conn.commit()
        
    cursor.close()
    cursor2.close()
    conn.close()


def getSemanticScore(poi,musician):
    N = 1512.0 # the total number of items
    score = (N + 1 - 101) / N # the default score
    conn = database.connect('eval-lamj')
    cursor = conn.cursor()
    cursor.execute('SELECT ranking FROM evaluation_results \
                    WHERE runId IN ( \
                    SELECT id FROM evaluation_runs \
                    WHERE poi="'+poi+'" AND ALGORITHM = "SimpleSpreading") \
                    AND musician="'+musician+'"')
    for row in cursor:
        # rank-based scoring function
        score = (N + 1 - row[0]) / N
        
    cursor.close()
    conn.close()
    
    return score


def getTagScore(poiId,trackId):
    N = 364.0 # total number of items
    score = 0.0
    conn = database.connect('eval-lamj')
    cursor = conn.cursor()
    cursor.execute('SELECT rank FROM \
                        (SELECT @rownum:=@rownum+1 rank, dbpedia_track \
                        FROM new_similarity, (SELECT @rownum:=0) r \
                        WHERE dbpedia_poi='+str(poiId)+' \
                        ORDER BY autoAll_similarity DESC) a \
                    WHERE dbpedia_track='+str(trackId))
    for row in cursor:
        # rank-based scoring function
        score = (N + 1 - row[0]) / N
        
    cursor.close()
    conn.close()
    
    return score


#####################################################################

if __name__ == "__main__":
    
    loadTagSimilarities()
    loadSemanticSimilarities()
    loadCombinedSimilarities()

    # loop over 25 POIs, get the list of tracks for each, compute Spearman's rank correlation
    rhoCoefficients = 0.0
    for i in range(101,126):
        rho, pvalue = scipy.stats.mstats.spearmanr(getTrackSimilarities(i, 'manual'),
                                                   getTrackSimilarities(i, 'auto'))
        print rho, pvalue
        rhoCoefficients += rho

    print 'average correlation ---> ',rhoCoefficients/25

