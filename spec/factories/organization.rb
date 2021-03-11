FactoryBot.define do
  factory :organization do
    login { 'treasure-data' }
    avatar_url { 'https//avatars.githubusercontent.com/u/119195?v=3' }
    type { 'Organization' }
    queued_at { Time.now }
    stargazers_count { 2405 }
    rank { 588 }
  end
end
